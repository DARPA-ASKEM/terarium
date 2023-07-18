package software.uncharted.terarium.hmiserver.resources;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

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


	/**
	 * @param sourceKey
	 * @param destinationKey
	 * @return
	 */
	protected SdkHttpResponse copyFile(String sourceKey, String destinationKey){
		//verify that our required S3 fields are set. If not, return an error
		if (bucket.isEmpty() || accessKeyId.isEmpty() || secretAccessKey.isEmpty() || region.isEmpty()) {
			log.error("S3 information not set. Cannot copy file.");
			return null;
		}

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));

		S3Client s3Client = S3Client.builder()
			.credentialsProvider(credentialsProvider)
			.region(Region.of(region.get()))
			.build();

		//TODO THIS IS A NASTY NASTY HACKATHON HACK. SIMULATION SERVICE RUNS INSIDE STAGING, AND TDS
		//RUNS LOCALLY. AS A RESULT, WE NEED TO COPY THE FILE FROM STAGING TO DEV. THIS IS A TEMPORARY
	 //TO MAKE IT WORK. WE SHOULD REMOVE THIS ONCE WE HAVE A BETTER SOLUTION.
		String overrideBucket = bucket.get().equals("askem-dev-data-service")? "askem-staging-data-service" : bucket.get();

		CopyObjectRequest copyReq = CopyObjectRequest.builder()
			.sourceBucket(overrideBucket)
			.sourceKey(sourceKey)
			.destinationBucket(bucket.get())
			.destinationKey(destinationKey)
			.build();

		CopyObjectResponse copyRes = s3Client.copyObject(copyReq);
		return copyRes.sdkHttpResponse();
	}


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

	protected String downloadStringFromS3(String key) {
		//verify that our required S3 fields are set. If not, return an error
		if (!bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot download file.");
			return null;
		}

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));

		S3Client client = S3Client.builder().region(Region.of(region.get())).credentialsProvider(credentialsProvider).build();

		GetObjectRequest request = GetObjectRequest.builder()
			.bucket(bucket.get()).key(key).build();

		ResponseInputStream<GetObjectResponse> s3objectResponse = client
			.getObject(request);

		String result = new BufferedReader(new InputStreamReader(s3objectResponse))
			.lines()
			.collect(Collectors.joining("\n"));

		return result;
	}

	protected byte[] downloadBytesFromS3(String key) {
		//verify that our required S3 fields are set. If not, return an error
		if (!bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot download file.");
			return null;
		}

		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));

		S3Client client = S3Client.builder().region(Region.of(region.get())).credentialsProvider(credentialsProvider).build();

		GetObjectRequest request = GetObjectRequest.builder()
			.bucket(bucket.get()).key(key).build();

		ResponseInputStream<GetObjectResponse> s3objectResponse = client
			.getObject(request);

		//now just get the bytes to return
		byte[] result = new byte[0];
		try {
			result = s3objectResponse.readAllBytes();
		} catch (IOException e) {
			log.error("Error reading bytes from S3 object", e);
			return null;
		}

		return result;
	}

}
