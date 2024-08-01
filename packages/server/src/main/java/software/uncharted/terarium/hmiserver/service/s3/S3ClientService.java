package software.uncharted.terarium.hmiserver.service.s3;

import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.uncharted.terarium.hmiserver.configuration.Config;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ClientService {

	protected final Config config;

	Map<String, S3Client> s3Clients = new HashMap<>();
	Map<String, S3Service> s3Services = new HashMap<>();
	Map<String, S3Presigner> s3Presigners = new HashMap<>();

	@PreDestroy
	public void destroy() {
		s3Services.values().forEach(S3Service::destroy);
		s3Presigners.values().forEach(S3Presigner::close);
	}

	/**
	 * Get {@link AwsCredentials} by id
	 *
	 * @param id The id of the credentials to get
	 * @return The credentials
	 * @throws IllegalArgumentException if the credentials could not be found
	 */
	AwsCredentials getAwsCredentials(final String id) {
		if (config.getAmazon().getCredentials().containsKey(id)) {
			final Config.AmazonCredentials credentials = config.getAmazon().getCredentials().get(id);
			return StaticCredentialsProvider.create(
				AwsBasicCredentials.create(credentials.getAccessKey(), credentials.getSecretKey())
			).resolveCredentials();
		}
		throw new IllegalArgumentException("Could not find credentials '" + id + "'");
	}

	/**
	 * Gets the default {@link S3Client}
	 *
	 * @return
	 */
	public S3Client getS3Client() {
		return getS3Client("default");
	}

	/**
	 * Get an {@link S3Client} by id
	 *
	 * @param id The id of the client to get
	 * @return The client, or null if the client could not be found/created
	 */
	public S3Client getS3Client(final String id) {
		return s3Clients.getOrDefault(id, initializeS3Client(id));
	}

	/**
	 * Gets the S3 pre-signer for the provided client
	 *
	 * @param id The id of the client to get the pre-signer for
	 * @return The pre-signer
	 */
	protected S3Presigner getS3Presigner(final String id) {
		if (!s3Presigners.containsKey(id)) {
			final Config.AmazonS3 s3Config = config.getAmazon().getS3().get(id);

			final S3Presigner preSigner;
			if (s3Config.getUrl() != null && !s3Config.getUrl().isEmpty()) {
				preSigner = S3Presigner.builder()
					.credentialsProvider(StaticCredentialsProvider.create(getAwsCredentials(s3Config.getCredentialsId())))
					.endpointOverride(URI.create(s3Config.getUrl()))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
					.region(Region.of(s3Config.getRegion()))
					.build();
			} else {
				preSigner = S3Presigner.builder()
					.credentialsProvider(StaticCredentialsProvider.create(getAwsCredentials(s3Config.getCredentialsId())))
					.region(Region.of(s3Config.getRegion()))
					.build();
			}

			s3Presigners.put(id, preSigner);
		}

		return s3Presigners.get(id);
	}

	/**
	 * Gets the default {@link S3Service}
	 *
	 * @return The service
	 */
	public S3Service getS3Service() {
		return getS3Service("default");
	}

	/**
	 * Get an {@link S3Service} by id
	 *
	 * @param id The id of the client to get
	 * @return The service
	 * @throws IllegalArgumentException if the client could not be found/created
	 */
	public S3Service getS3Service(final String id) {
		if (getS3Client(id) != null) {
			s3Services.putIfAbsent(
				id,
				new S3Service(config, getS3Client(id), getS3Presigner(id), config.getMultipartFileBufferSize())
			);
			return s3Services.get(id);
		}
		throw new IllegalArgumentException("Could not find S3 client '" + id + "'");
	}

	/**
	 * Initialize an {@link S3Client} by id, and store it in {@link #s3Clients}
	 *
	 * @param id The id of the client to initialize
	 * @return The initialized client, or null if the client could not be
	 *         initialized
	 */
	S3Client initializeS3Client(final String id) {
		if (config.getAmazon().getS3().containsKey(id)) {
			final Config.AmazonS3 s3Config = config.getAmazon().getS3().get(id);
			final AwsCredentials credentials = getAwsCredentials(s3Config.getCredentialsId());
			if (s3Config.getUrl() != null && !s3Config.getUrl().isEmpty()) {
				s3Clients.put(
					id,
					S3Client.builder()
						.credentialsProvider(StaticCredentialsProvider.create(credentials))
						.region(Region.of(s3Config.getRegion()))
						.forcePathStyle(true)
						.endpointOverride(URI.create(s3Config.getUrl()))
						.build()
				);
			} else {
				s3Clients.put(
					id,
					S3Client.builder()
						.credentialsProvider(StaticCredentialsProvider.create(credentials))
						.region(Region.of(s3Config.getRegion()))
						.build()
				);
			}
			return s3Clients.get(id);
		} else {
			log.error("Could not find S3 client {}", id);
			throw new IllegalArgumentException("Could not find S3 client '" + id + "'");
		}
	}
}
