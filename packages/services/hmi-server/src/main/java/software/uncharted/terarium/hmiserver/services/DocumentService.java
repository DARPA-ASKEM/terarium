package software.uncharted.terarium.hmiserver.services;

import software.uncharted.terarium.hmiserver.models.XDD.Document;
import software.uncharted.terarium.hmiserver.models.XDD.Extraction;
import software.uncharted.terarium.hmiserver.models.XDD.XDDArticlesResponseOK;
import software.uncharted.terarium.hmiserver.models.XDD.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.models.XDD.XDDResponse;
import software.uncharted.terarium.hmiserver.models.XDD.XDDSearchPayload;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class DocumentService {

	// to-be-loaded from application.properties
	private String DOCUMENTS_BASE_URL = "";
	private String EXTRACTIONS_BASE_URL = "";
	private String QueryParametersEncoder = "UTF-8";

	// create a client
	HttpClient client = HttpClient.newHttpClient();

	public DocumentService() {
		loadServiceConfigParams();
	}

	private void loadServiceConfigParams() {
		// check if any of the XDD API URLs is empty
		if (DOCUMENTS_BASE_URL.isEmpty()) {
			Properties properties = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try (InputStream inputStream = loader.getResourceAsStream("application.properties")) {
					properties.load(inputStream);

					DOCUMENTS_BASE_URL = properties.getProperty("xdd.document.base.url");
					EXTRACTIONS_BASE_URL = properties.getProperty("xdd.extraction.base.url");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Document> getDocuments(String jsonPayload) {
		List<Document> list = new ArrayList<>();

		String url = DOCUMENTS_BASE_URL;

		if (!jsonPayload.isEmpty()) {
			try {
				XDDSearchPayload payload = new ObjectMapper()
					.readValue(jsonPayload, XDDSearchPayload.class);
				if (payload.doi != null) {
					// if doi or title is provided, all other params are ignored
					url += "doi=" + URLEncoder.encode(payload.doi, QueryParametersEncoder);
				} else if (payload.title != null) {
					url += "title=" + URLEncoder.encode(payload.title, QueryParametersEncoder);
				} else {
					// doi/title are not given, then apply search parameters
					if (payload.term != null) {
						url += "term=" + URLEncoder.encode(payload.term, QueryParametersEncoder);
						url += "&";
					}
				}
				// continue with the XDD endpoint call
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// create a request
		var request = HttpRequest.newBuilder(
					URI.create(url))
			.header("accept", "application/json")
			.build();

		// use the client to send the request
		// @NOTE: we may as well use send the request sync, but this initial implementation uses async
		var responseFuture = client.sendAsync(request, BodyHandlers.ofString());

		// We can do other things here while the request is in-flight

		// This blocks until the request is complete
		try {
			var response = responseFuture.get();

			String responseBodyStr = response.body();
			try {
				XDDResponse<XDDArticlesResponseOK> typedResponse = new ObjectMapper()
					// .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.readValue(responseBodyStr, new TypeReference<XDDResponse<XDDArticlesResponseOK>>() {});

				// NOTE that if no params are provided in the search payload,
				//  then the XDD API results will not be valid (and the mapping will not fail)
				if (typedResponse.success != null && typedResponse.success.data != null) {
					for (Document doc : typedResponse.success.data) {
						list.add(doc);
					}
				}
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}

		return list;
	}

	public List<Extraction> getExtractions(String jsonPayload) {
		List<Extraction> list = new ArrayList<>();

		String url = EXTRACTIONS_BASE_URL;

		if (!jsonPayload.isEmpty()) {
			try {
				XDDSearchPayload payload = new ObjectMapper()
					.readValue(jsonPayload, XDDSearchPayload.class);
				// @TODO: validate that a proper doi is given
				if (payload.doi != null) {
					// add the doi to the query
					url += "doi=" + payload.doi;

					// continue with the requsting the extractions

					// create a request
					var request = HttpRequest.newBuilder(
								URI.create(url))
						.header("accept", "application/json")
						.build();

					// use the client to send the request
					// @NOTE: we may as well use send the request sync,
					//        but this initial implementation uses async
					var responseFuture = client.sendAsync(request, BodyHandlers.ofString());

					// We can do other things here while the request is in-flight

					// This blocks until the request is complete
					var response = responseFuture.get();

					String responseBodyStr = response.body();
						XDDResponse<XDDExtractionsResponseOK> typedResponse = new ObjectMapper()
							// .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
							.readValue(responseBodyStr, new TypeReference<XDDResponse<XDDExtractionsResponseOK>>() {});

					// NOTE that if no params are provided in the search payload,
					//  then the XDD API results will not be valid (and the mapping will not fail)
					if (typedResponse.success != null && typedResponse.success.data != null) {
						for (Extraction ext : typedResponse.success.data) {
							list.add(ext);
						}
					}
				}

			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			}
		}

		return list;
	}
}
