package software.uncharted.terarium.hmiserver.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Configuration
@ConfigurationProperties(prefix = "terarium")
@EnableScheduling
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Config {

	/**
	 * The base url of the deployed application. Eg/ http://localhost:5173 or
	 * https://myapp.uncharted.software
	 */
	String baseUrl;

	/** A list of patterns for service requests that can be used via basic auth */
	List<String> serviceRequestPatterns;

	/**
	 * Max age of the cache headers in seconds.
	 */
	Integer cacheHeadersMaxAge = 60 * 60 * 24;

	/**
	 * A list of unauthenticated {@link org.springframework.util.AntPathMatcher}
	 * patterns for urls that should not be
	 * authenticated via Spring Security
	 */
	List<String> unauthenticatedUrlPatterns;

	/** Keycloak configuration */
	Keycloak keycloak;

	/** Caching configuration */
	Caching caching;

	/** Configuration values that are passed to the client */
	ClientConfig clientConfig;

	/**
	 * If queues should be declared durable. IF running Rabbit inside docker, this
	 * should be false
	 */
	Boolean durableQueues = false;

	/** The buffer size when uploading large files to the server */
	Integer multipartFileBufferSize = 50 * 1024 * 1024;

	/**
	 * The encryption key used to encrypt the download urls for retrieving files
	 * from the server
	 */
	String presignedUrlEncryptionKey;

	/** The number of seconds that a presigned url signature is valid for */
	Integer presignedUrlExpirationSeconds = 5;

	/** If true, enable swagger */
	Boolean enableSwagger;

	/** Amazon configuration */
	Amazon amazon;

	/*
	 * S3 Storage related config
	 */
	String fileStorageS3BucketName;
	String fileStorageS3ClientName;
	String datasetPath;
	String resultsPath;
	String artifactPath;
	String documentPath;
	String codePath;
	String imagePath;

	@Data
	@Accessors(chain = true)
	public static class Caching {

		/**
		 * If true, clear the cache on startup. Should be false in production
		 * environments
		 */
		Boolean clearOnStartup;
	}

	@Data
	@Accessors(chain = true)
	public static class Keycloak {

		/**
		 * The url of the keycloak server. eg/ http://localhost:8081 or
		 * https://keycloak.uncharted.software
		 */
		String url;
		/** The realm name to use for authentication */
		String realm;

		/** the realm where the admin-cli lives */
		String adminRealm;

		/** The client id in keycloak */
		String clientId;

		/** The client id for the admin connection */
		String adminClientId;

		/** the admin username */
		String adminUsername;

		/** the admin password */
		String adminPassword;
	}

	@Data
	@Accessors(chain = true)
	@TSModel
	public static class ClientConfig implements Serializable {

		/**
		 * The base url of the deployed application. Mirror of {@link Config#baseUrl}
		 */
		String baseUrl;

		/** The url of the help documentation */
		String documentationUrl;

		/**
		 * If true, we will log all client-side errors to the server. This is useful for
		 * debugging, but should be false
		 */
		Boolean clientLogShippingEnabled;

		/**
		 * The interval, in milliseconds, at which we will ship client-side logs to the
		 * server
		 */
		Long clientLogShippingIntervalMillis;

		/**
		 * The interval, in milliseconds, at which we will send a heartbeat to connected
		 * clients for server-side-events
		 */
		Long sseHeartbeatIntervalMillis;
	}

	@Data
	@Accessors(chain = true)
	public static class AmazonCredentials {

		String accessKey;
		String secretKey;
	}

	@Data
	@Accessors(chain = true)
	public static class AmazonS3 {

		String region;
		String url;
		String credentialsId;
	}

	@Data
	@Accessors(chain = true)
	public static class Amazon {

		Map<String, AmazonCredentials> credentials;
		Map<String, AmazonS3> s3;
	}
}
