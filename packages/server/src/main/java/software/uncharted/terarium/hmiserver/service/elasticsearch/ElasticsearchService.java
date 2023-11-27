package software.uncharted.terarium.hmiserver.service.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;

import java.io.IOException;
import java.net.URI;

@Service
@Data
@Slf4j
public class ElasticsearchService {

	private final ObjectMapper mapper;

	private final RestTemplateBuilder restTemplateBuilder;

	private RestTemplate restTemplate;

	private ElasticsearchClient client = null;

	private RestHighLevelClient restHighLevelClient = null;

	private final ElasticsearchConfiguration config;

	protected RestTemplate getRestTemplate() {
		if (restTemplate == null) {
			initRestTemplate();
		}
		return restTemplate;
	}

	private void initRestTemplate() {
		RestTemplateBuilder builder = getRestTemplateBuilder();
		if (config.isAuthEnabled()) {
			builder = builder.basicAuthentication(config.getUsername(), config.getPassword());
		}
		this.restTemplate = builder.build();
	}

	@PostConstruct
	public void init() throws IOException {
		final RestClientBuilder httpClientBuilder = RestClient.builder(
			HttpHost.create(config.getUrl())
		);

		// Create the HLRC
		this.restHighLevelClient = new RestHighLevelClient(httpClientBuilder);

		// Create the new Java Client with the same low level client
		final ElasticsearchTransport transport = new RestClientTransport(
			restHighLevelClient.getLowLevelClient(),
			new JacksonJsonpMapper(mapper)
		);

		this.client = new ElasticsearchClient(transport);
		try {
			client.ping();
		} catch (final IOException e) {
			log.error("Unable to ping Elasticsearch Rest Client", e);
		}
	}

	/**
	 * Create all indices that are not already present in the cluster
	 *
	 * @throws IOException If there is an error creating the index
	 * @return True if the index exists, false otherwise
	 */
	public boolean containsIndex(final String indexName) {
		boolean exists = false;
		try {
			exists = client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
		} catch (final IOException e) {
			log.error("Error checking existence of index {}", indexName, e);
		}
		return exists;
	}


	public void createIndex(final String index) throws IOException {
		final CreateIndexRequest req = new CreateIndexRequest(index);
		restHighLevelClient.indices().create(req, RequestOptions.DEFAULT);
	}

	/**
	 * Returns true if the ES cluster contains the index template with the provided name, false otherwise
	 *
	 * @param name The name of the index template to check existence for
	 * @return True if the index template is contained in the cluster, false otherwise
	 */
	public boolean containsIndexTemplate(final String name) {
		final IndexTemplatesExistRequest request = new IndexTemplatesExistRequest(name);
		boolean exists = false;
		try {
			exists = restHighLevelClient.indices().existsTemplate(request, RequestOptions.DEFAULT);
		} catch (final ElasticsearchStatusException e) {
			if (e.status() != RestStatus.NOT_FOUND) {
				log.error("Error checking existence of template, unexpected ElasticsearchStatusException result {}", name, e);
			}
		} catch (final IOException e) {
			log.error("Error checking existence of template {}", name, e);
		}
		return exists;
	}

	/**
	 * Put an index template to the cluster
	 *
	 * @param name         The name of the index template
	 * @param templateJson The index template json string
	 * @return True if the index template was successfully added, false otherwise
	 */
	public boolean putIndexTemplate(final String name, final String templateJson) {
		return putTyped(name, templateJson, "index template", "_index_template");
	}

	/**
	 * Check if the cluster contains the pipeline with the provided name
	 *
	 * @param name The name of the pipeline to check existence for
	 * @return True if the pipeline is contained in the cluster, false otherwise
	 */
	public boolean containsPipeline(final String name) {
		final GetPipelineRequest request = new GetPipelineRequest(name);
		boolean exists = false;
		try {
			exists = restHighLevelClient.ingest().getPipeline(request, RequestOptions.DEFAULT).isFound();
		} catch (final IOException e) {
			log.error("Error checking existence of pipeline {}", name, e);
		}
		return exists;
	}

	/**
	 * Put a pipeline to the cluster
	 *
	 * @param name         The name of the pipeline
	 * @param pipelineJson The pipeline json string
	 * @return True if the pipeline was successfully added, false otherwise
	 */
	public boolean putPipeline(final String name, final String pipelineJson) {
		return putTyped(name, pipelineJson, "pipeline", "_ingest/pipeline");
	}

	/**
	 * Put a typed object to the cluster
	 *
	 * @param name
	 * @param typedJson
	 * @param typeName
	 * @param indexName
	 * @return True if the object was successfully added, false otherwise
	 */
	private boolean putTyped(final String name, final String typedJson, final String typeName, final String indexName) {
		log.info("Putting " + typeName + ": {}", name);

		try {
			final HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			final HttpEntity<String> entity = new HttpEntity<>(typedJson, headers);
			final ResponseEntity<JsonNode> response = getRestTemplate().exchange(
				new URI(config.getUrl() + "/" + indexName + "/" + name),
				HttpMethod.PUT, entity,
				JsonNode.class);
			final JsonNode body = response.getBody();
			if (body != null) {
				return body.at("/acknowledged").asBoolean();
			}
		} catch (final Exception e) {
			log.error("Error putting " + typeName + " {}", name, e);
		}
		return false;
	}
}
