package software.uncharted.terarium.hmiserver.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.core.ProgressListener;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.s3.S3Object;
import software.uncharted.terarium.hmiserver.models.s3.S3ObjectListing;
import software.uncharted.terarium.hmiserver.models.s3.UploadProgress;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.service.s3.S3Service;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class S3FileController {

	private final S3ClientService s3ClientService;
	private final ClientEventService clientEventService;
	private final CurrentUserService currentUserService;
	private final Config config;

	@Value("${terarium.file-storage-s3-bucket-name:terarium-file-storage}")
	String FILE_STORAGE_DEFAULT_BUCKET_NAME;

	@Value("${terarium.file-storage-s3-client-name:default}")
	String FILE_STORAGE_DEFAULT_S3_CLIENT_NAME;

	@PostConstruct
	void init() {
		final S3Service s3Service = s3ClientService.getS3Service(FILE_STORAGE_DEFAULT_S3_CLIENT_NAME);
		if (!s3Service.bucketExists(FILE_STORAGE_DEFAULT_BUCKET_NAME)) {
			s3Service.createBucket(FILE_STORAGE_DEFAULT_BUCKET_NAME);
		}
	}

	/**
	 * Get the default bucket name
	 *
	 * @return The default bucket name
	 */
	@GetMapping("/buckets/default")
	public ResponseEntity<String> getDefaultBucket() {
		return ResponseEntity.ok(FILE_STORAGE_DEFAULT_BUCKET_NAME);
	}

	/**
	 * List buckets in S3
	 *
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link ResponseEntity} containing a list of bucket names on success
	 */
	@GetMapping("/buckets")
	public ResponseEntity<List<String>> getBuckets(
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) {
		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		return ResponseEntity.ok(s3Service.listBuckets());
	}

	/**
	 * Gets the signature for download an a file from S3.
	 *
	 * <p>We use this to verify that the user has permission to download the file. If permission checks need to be
	 * added, they should be done in this method and it should return a 403 Forbidden
	 *
	 * @param bucket The bucket to download from
	 * @param key The key to download
	 * @param s3Id The ID of the S3 client to use
	 * @return The signature of this download that is used as the signature query parameter when downloading the file
	 */
	@GetMapping("/sign")
	public ResponseEntity<String> getDownloadSignature(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam("key") final String key,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) {
		return ResponseEntity.ok(S3Service.getSignature(bucket, key, s3Id, config.getPresignedUrlEncryptionKey()));
	}

	/**
	 * Get a file from S3
	 *
	 * @param bucket The bucket to get the file from
	 * @param key The key of the file to get
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link StreamingResponseBody} that streams the file back to the client as it is read from S3
	 */
	@GetMapping
	public ResponseEntity<StreamingResponseBody> getFile(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam("key") final String key,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id,
		@RequestParam(value = "signature", required = false) final String signature
	) {
		if (signature == null || signature.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signature is required");
		}

		if (
			!S3Service.validateSignature(
				bucket,
				key,
				s3Id,
				signature,
				config.getPresignedUrlEncryptionKey(),
				config.getPresignedUrlExpirationSeconds()
			)
		) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
		}

		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		if (!s3Service.bucketExists(bucket)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket does not exist");
		}
		final S3Object s3Object = s3Service.getObjectInformation(bucket, key);

		// Stream object back to client
		final ResponseInputStream<GetObjectResponse> responseStream = s3Service.getObject(bucket, key);
		final StreamingResponseBody body = outputStream -> {
			int numberOfBytesToWrite;
			final byte[] data = new byte[config.getMultipartFileBufferSize()];
			while ((numberOfBytesToWrite = responseStream.read(data, 0, data.length)) != -1) {
				outputStream.write(data, 0, numberOfBytesToWrite);
			}
		};
		return ResponseEntity.ok()
			.header("Content-Disposition", "attachment; filename=\"" + key + "\"")
			.header("Content-Length", String.valueOf(s3Object.getSizeInBytes()))
			.body(body);
	}

	/**
	 * Delete a file from S3
	 *
	 * @param bucket The bucket to delete the file from
	 * @param key The key of the file to delete
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link ResponseEntity} with no body on success
	 */
	@DeleteMapping
	public ResponseEntity<Void> deleteFile(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam("key") final String key,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) {
		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		if (!s3Service.bucketExists(bucket)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket does not exist");
		}
		s3Service.deleteObject(bucket, key);
		return ResponseEntity.ok().build();
	}

	/**
	 * Directly puts a byte stream into S3. This is done for files <5mb in size as a single part upload. Multi-part
	 * uploads should be done using {@link #createMultipartUpload(String, String, String)} and
	 * {@link #putMultipartFile(HttpServletRequest, String, String, String, String)} This is a required method for the
	 * S3 client to work with the AWS SDK.
	 *
	 * @param bucket The bucket to put the file in
	 * @param key The key to put the file at
	 * @param s3Id The ID of the S3 client to use
	 * @param bytes The bytes of the file to put
	 * @return A {@link ResponseEntity} containing a {@link PutObjectResponse} on success
	 */
	@PutMapping(consumes = "application/octet-stream")
	public ResponseEntity<Void> putFile(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam("key") final String key,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id,
		@RequestBody final byte[] bytes
	) {
		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		if (!s3Service.bucketExists(bucket)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket does not exist");
		}
		s3Service.putObject(bucket, key, bytes);
		return ResponseEntity.ok(null);
	}

	/**
	 * List files in a bucket
	 *
	 * @param bucket The bucket to list files from
	 * @param prefix The prefix to filter files by
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link ResponseEntity} containing a {@link S3ObjectListing} on success
	 * @throws IOException If there is an error listing the files
	 */
	@GetMapping("/list")
	public ResponseEntity<S3ObjectListing> listFiles(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam(value = "prefix", required = false, defaultValue = "") final String prefix,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) throws IOException {
		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		return ResponseEntity.ok(s3Service.listObjects(bucket, prefix));
	}

	/**
	 * Create a multipart upload
	 *
	 * @param bucket The bucket to upload to
	 * @param key The key to upload to
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link ResponseEntity} containing the upload ID on success
	 * @throws IOException If there is an error creating the multipart upload
	 */
	@PutMapping("/upload")
	public ResponseEntity<String> createMultipartUpload(
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam("key") final String key,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) throws IOException {
		final S3Service s3Service = s3ClientService.getS3Service(s3Id);
		final CreateMultipartUploadResponse response = s3Service.createMultipartUpload(bucket, key);
		log.info("Created multipart upload with ID {}", response.uploadId());
		return ResponseEntity.ok(response.uploadId());
	}

	/**
	 * Upload a file to S3
	 *
	 * @param request The request containing the file to upload
	 * @param uploadId The ID of the upload to add the file to
	 * @param bucket The bucket to upload to
	 * @param prefix The prefix to upload the file to
	 * @param s3Id The ID of the S3 client to use
	 * @return A {@link ResponseEntity} with no body on success
	 * @throws IOException If there is an error uploading the file
	 */
	@PutMapping(value = "upload/{uploadId}", consumes = "multipart/form-data")
	public ResponseEntity<Void> putMultipartFile(
		final HttpServletRequest request,
		@PathVariable("uploadId") final String uploadId,
		@RequestParam(
			value = "bucket",
			required = false,
			defaultValue = "${terarium.file-storage-s3-bucket-name:terarium-file-storage}"
		) final String bucket,
		@RequestParam(value = "prefix", required = false, defaultValue = "") final String prefix,
		@RequestParam(
			value = "id",
			required = false,
			defaultValue = "${terarium.file-storage-s3-client-name:default}"
		) final String s3Id
	) throws IOException {
		final S3Service service = s3ClientService.getS3Service(s3Id);
		if (!service.bucketExists(bucket)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bucket does not exist");
		}
		final String contentLengthStr = request.getHeader("Content-Length");
		if (contentLengthStr == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content-Length header is required");
		}

		// Create a progress listener
		final AtomicInteger lastPercentReport = new AtomicInteger(0);
		final ProgressListener progressListener = (bytesRead, contentLength, items) -> {
			final int percent = (int) ((bytesRead * 100.f) / contentLength);
			if (percent > lastPercentReport.get()) {
				lastPercentReport.set(percent);
				final ClientEvent<UploadProgress> event = ClientEvent.<UploadProgress>builder()
					.type(ClientEventType.FILE_UPLOAD_PROGRESS)
					.data(new UploadProgress().setUploadId(uploadId).setPercentComplete(percent))
					.build();
				clientEventService.sendToUser(event, currentUserService.get().getId());
			}
		};

		final JakartaServletFileUpload upload = new JakartaServletFileUpload();
		upload.setProgressListener(progressListener);
		upload
			.getItemIterator(request)
			.forEachRemaining(item -> {
				final InputStream stream = item.getInputStream();
				final String name = item.getName();
				final S3Service s3Service = s3ClientService.getS3Service(s3Id);
				s3Service.putObject(uploadId, bucket, prefix + name, stream);
			});
		return ResponseEntity.ok().build();
	}
}
