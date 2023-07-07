package software.uncharted.terarium.hmiserver.resources;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;

@Slf4j
public abstract class DataStorageResource {

	@ConfigProperty(name = "aws.bucket")
	Optional<String> bucket;

	@ConfigProperty(name = "aws.access_key_id")
	Optional<String> accessKeyId;

	@ConfigProperty(name = "aws.secret_access_key")
	Optional<String> secretAccessKey;

	@ConfigProperty(name = "aws.region")
	Optional<String> region;


	protected SdkHttpResponse uploadBytesToS3(String key, byte[] data) {

		//verify that our required S3 fields are set. If not, return an error
		if (!bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot upload file.");
			return null;
		}

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));


		S3Client s3Client = S3Client.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region.get()))
				.build();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket.get())
				.key(key)
				.build();
		return s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data)).sdkHttpResponse();
	}

	protected SdkHttpResponse uploadStringToS3(String key, String data) {
		//verify that our required S3 fields are set. If not, return an error
		if (!bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot upload file.");
			return null;
		}

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));


		S3Client s3Client = S3Client.builder()
			.credentialsProvider(credentialsProvider)
			.region(Region.of(region.get()))
			.build();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket.get())
			.key(key)
			.build();
		return s3Client.putObject(putObjectRequest, RequestBody.fromString(data)).sdkHttpResponse();
	}

}
