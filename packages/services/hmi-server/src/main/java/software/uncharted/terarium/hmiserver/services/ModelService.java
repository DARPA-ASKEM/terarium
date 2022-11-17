package software.uncharted.terarium.hmiserver.services;

import software.uncharted.terarium.hmiserver.models.Model;
import java.util.List;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@ApplicationScoped
public class ModelService {
	private String DATA_SERVICE_BASE_URL = "";

	public ModelService() {
		loadServiceConfigParams();
	}

	private void loadServiceConfigParams() {
		if (DATA_SERVICE_BASE_URL.isEmpty()) {
			Properties properties = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try (InputStream inputStream = loader.getResourceAsStream("application.properties")) {
					properties.load(inputStream);
					DATA_SERVICE_BASE_URL = properties.getProperty("data-service/mp-rest/url");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Model> getModels() {
		List<Model> list = new ArrayList<Model>();
		HttpClient client = HttpClient.newHttpClient();
		// create a request
		String url = DATA_SERVICE_BASE_URL + "/models";
		var request = HttpRequest.newBuilder(URI.create(url))
			.header("accept", "application/json")
			.build();
		try {
			var response = client.send(request, BodyHandlers.ofString());
			var responseBodyStr = response.body();
			var typedResponse = new ObjectMapper()
				.registerModule(new Jdk8Module())
				.readValue(responseBodyStr, new TypeReference<List<Model>>() {});
			for (Model model : typedResponse) {
				list.add(model);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return list;
	}
}
