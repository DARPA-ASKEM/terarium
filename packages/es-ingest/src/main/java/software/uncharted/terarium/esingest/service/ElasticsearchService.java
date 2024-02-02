package software.uncharted.terarium.esingest.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfigParam;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsIndexTemplateRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.ingest.GetPipelineRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Service
@Data
@Slf4j
public class ElasticsearchService {

	private final ObjectMapper mapper;

	private final RestTemplateBuilder restTemplateBuilder;

	private RestTemplate restTemplate;

	private ElasticsearchClient client = null;

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
	public void init() {
		log.info("Connecting elasticsearch client to: {}", config.getUrl());

		final RestClientBuilder httpClientBuilder = RestClient.builder(
				HttpHost.create(config.getUrl()));

		if (config.isAuthEnabled()) {
			String auth = config.getUsername() + ":" + config.getPassword();
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
			Header header = new BasicHeader("Authorization", "Basic " + encodedAuth);

			httpClientBuilder.setDefaultHeaders(new Header[] { header });
		}

		final RestClient httpClient = httpClientBuilder.build();

		// Now you can create an ElasticsearchTransport object using the RestClient
		final ElasticsearchTransport transport = new RestClientTransport(httpClient, new JacksonJsonpMapper(mapper));

		client = new ElasticsearchClient(transport);

		try {
			client.ping();
		} catch (final IOException e) {
			log.error("Unable to ping Elasticsearch Rest Client", e);
		}
	}

	/**
	 * Create all indices that are not already present in the cluster
	 *
	 * @return True if the index exists, false otherwise
	 */
	public boolean containsIndex(final String indexName) throws IOException {
		return client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
	}

	/**
	 * Check for the existence of a document in an index by id.
	 *
	 * @return True if the index exists, false otherwise
	 */
	public boolean contains(final String indexName, final String id) throws IOException {
		final GetRequest req = new GetRequest.Builder()
				.index(indexName)
				.id(id)
				.source(new SourceConfigParam.Builder().fetch(false).build())
				.build();

		GetResponse<JsonNode> response = client.get(req, JsonNode.class);
		return response.found();
	}

	/**
	 * Create the provided index.
	 *
	 * @param index
	 * @throws IOException
	 */
	public void createIndex(final String index) throws IOException {

		final CreateIndexRequest req = new CreateIndexRequest.Builder().index(index).build();

		client.indices().create(req);
	}

	/**
	 * Create the provided index if it doesn't exist, if it does, delete it and
	 * re-create it.
	 *
	 * @param index
	 * @throws IOException
	 */
	public void createOrEnsureIndexIsEmpty(final String index) throws IOException {
		if (containsIndex(index)) {
			deleteIndex(index);
		}
		createIndex(index);
	}

	/**
	 * Returns true if the ES cluster contains the index template with the provided
	 * name, false otherwise
	 *
	 * @param name The name of the index template to check existence for
	 * @return True if the index template is contained in the cluster, false
	 *         otherwise
	 */
	public boolean containsIndexTemplate(final String name) throws IOException {
		final ExistsIndexTemplateRequest req = new ExistsIndexTemplateRequest.Builder().name(name).build();

		return client.indices().existsIndexTemplate(req).value();
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
	 * Check if the cluster contains the pipeline with the provided id
	 *
	 * @param id The name of the pipeline to check existence for
	 * @return True if the pipeline is contained in the cluster, false otherwise
	 */
	public boolean containsPipeline(final String id) throws IOException {
		final GetPipelineRequest req = new GetPipelineRequest.Builder().id(id).build();

		return client.ingest().getPipeline(req).result().containsKey(id);
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
	 * @param name      The name of the object
	 * @param typedJson The object json string
	 * @param typeName  The type of the object
	 * @param indexName The index to put the object in
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

	/**
	 * Search an index using a provided query (can be null for no query)
	 *
	 * @param <T>    The type of the document
	 * @param req    - The search request
	 * @param tClass The class of the document
	 * @return A list of found documents.
	 */
	public <T> List<T> search(final SearchRequest req, final Class<T> tClass) throws IOException {
		log.info("Searching: {}", req.index());

		final List<T> docs = new ArrayList<>();
		final SearchResponse<T> res = client.search(req, tClass);
		for (final Hit<T> hit : res.hits().hits()) {
			docs.add(hit.source());
		}
		return docs;
	}

	/**
	 * Add a document to an index.
	 *
	 * @param <T>      The type of the document
	 * @param index    The index to add the document to
	 * @param id       The id of the document
	 * @param document The document to add
	 */
	public <T> void index(final String index, final String id, final T document) throws IOException {
		log.info("Indexing: {} into {}", id, index);

		final IndexRequest<T> req = new IndexRequest.Builder<T>()
				.index(index)
				.id(id)
				.document(document)
				.refresh(Refresh.WaitFor)
				.build();

		client.index(req);
	}

	/**
	 * Remove a document from an index.
	 *
	 * @param index The index to remove the document from
	 * @param id    The id of the document to remove
	 */
	public void delete(final String index, final String id) throws IOException {
		log.info("Deleting: {} from {}", id, index);

		final DeleteRequest req = new DeleteRequest.Builder()
				.index(index)
				.id(id)
				.refresh(Refresh.WaitFor)
				.build();

		client.delete(req);
	}

	/**
	 * Remove an index.
	 *
	 * @param index The index to remove
	 */
	public void deleteIndex(final String index) throws IOException {
		log.info("Deleting index: {}", index);

		DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder()
				.index(index)
				.build();

		client.indices().delete(deleteRequest);
	}

	/**
	 * Get a single document by id.
	 *
	 * @param <T>    The type of the document
	 * @param index  The index to get the document from
	 * @param id     The id of the document to get
	 * @param tClass The class of the document
	 * @return The document if found, null otherwise
	 */
	public <T> T get(final String index, final String id, final Class<T> tClass) throws IOException {
		log.info("Getting: {} from {}", id, index);

		final GetRequest req = new GetRequest.Builder()
				.index(index)
				.id(id)
				.build();

		final GetResponse<T> res = client.get(req, tClass);
		if (res.found()) {
			return res.source();
		}
		return null;
	}

	@Data
	static public class BulkOpResponse {
		private List<String> errors;
		private long took;
	}

	public <Output extends IOutputDocument<?>> BulkOpResponse bulkIndex(String index, List<Output> docs)
			throws IOException {
		BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

		for (Output doc : docs) {
			bulkRequest.operations(op -> op
					.index(idx -> idx
							.index(index)
							.id(doc.getId().toString())
							.document(doc)));
		}

		BulkResponse bulkResponse = client.bulk(bulkRequest.build());

		List<String> errors = new ArrayList<>();
		if (bulkResponse.errors()) {
			for (BulkResponseItem item : bulkResponse.items()) {
				ErrorCause error = item.error();
				if (error != null) {
					errors.add(error.reason());
				}
			}
		}

		BulkOpResponse r = new BulkOpResponse();
		r.setErrors(errors);
		r.setTook(bulkResponse.took());
		return r;
	}

	public <Output extends IOutputDocument<?>> BulkOpResponse bulkUpdate(String index, List<Output> docs)
			throws IOException {
		BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

		List<BulkOperation> operations = new ArrayList<>();
		for (Output doc : docs) {
			UpdateOperation<Object, Object> updateOperation = new UpdateOperation.Builder<Object, Object>()
					.index(index)
					.id(doc.getId().toString())
					.action(a -> a.doc(doc))
					.build();

			BulkOperation operation = new BulkOperation.Builder().update(updateOperation).build();
			operations.add(operation);
		}
		// Add the BulkOperation to the BulkRequest
		bulkRequest.operations(operations);

		BulkResponse bulkResponse = client.bulk(bulkRequest.build());

		List<String> errors = new ArrayList<>();
		if (bulkResponse.errors()) {
			for (BulkResponseItem item : bulkResponse.items()) {
				ErrorCause error = item.error();
				if (error != null) {
					errors.add(error.reason());
				}
			}
		}

		BulkOpResponse r = new BulkOpResponse();
		r.setErrors(errors);
		r.setTook(bulkResponse.took());
		return r;
	}

	@Data
	static public class ScriptedUpdatedDoc {
		String id;
		Map<String, JsonData> params;
	}

	public BulkOpResponse bulkScriptedUpdate(String index, String script, List<ScriptedUpdatedDoc> docs)
			throws IOException {

		BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

		List<BulkOperation> operations = new ArrayList<>();
		for (ScriptedUpdatedDoc doc : docs) {
			BulkOperation operation = new BulkOperation.Builder().update(u -> u
					.id(doc.getId())
					.index(index)
					.retryOnConflict(10)
					.action(action -> action
							.script(s -> s
									.inline(inlineScript -> inlineScript
											.lang("painless")
											.params(doc.getParams())
											.source(script)))))
					.build();

			operations.add(operation);
		}

		// Add the BulkOperation to the BulkRequest
		bulkRequest.operations(operations);

		BulkResponse bulkResponse = client.bulk(bulkRequest.build());

		List<String> errors = new ArrayList<>();
		if (bulkResponse.errors()) {
			for (BulkResponseItem item : bulkResponse.items()) {
				ErrorCause error = item.error();
				if (error != null) {
					errors.add(error.reason());
				}
			}
		}

		BulkOpResponse r = new BulkOpResponse();
		r.setErrors(errors);
		r.setTook(bulkResponse.took());
		return r;
	}

}
