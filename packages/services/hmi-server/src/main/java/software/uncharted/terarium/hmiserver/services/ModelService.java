package software.uncharted.terarium.hmiserver.services;

import software.uncharted.terarium.hmiserver.models.Model;
import java.util.List;
import java.io.IOException;
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
	private String MODELS_BASE_URL = "http://localhost:8001/models";

	public List<Model> getModels() {
		List<Model> list = new ArrayList<Model>();
		HttpClient client = HttpClient.newHttpClient();
		// create a request
		var request = HttpRequest.newBuilder(URI.create(MODELS_BASE_URL))
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

	public List<Model> getMockModels() {
		List<Model> list = new ArrayList<Model>();
		//
		// fill the list of models with mock data
		//
		Model m1 = new Model();
		m1.id = "1";
		m1.description = "CHIME is a modified SIR model of outbreak progression that is limited to short term forecasting. It is only applicable during the period prior to a region’s peak infections, and it accounts only for a single significant social distancing policy.";
		m1.status = "ready";
		m1.category = "Mechanistic - compartmental";
		m1.name = "CHIME";
		m1.type = "model";
		m1.source = "Jeffrey Kantor, Notre Dame";
		list.add(m1);
		Model m2 = new Model();
		m2.id = "2";
		m2.description = "Short Colab notebook in Python used to teach SIR at Notre Dame. Basic SIR/SEIR with mitigation measures like social distancing";
		m2.status = "ready";
		m2.category = "Mechanistic - compartmental";
		m2.name = "University of Notre Dame CBE 30338 Class Model";
		m2.type = "model";
		m2.source = "Penn Medicine";
		list.add(m2);
		Model m3 = new Model();
		m3.id = "3";
		m3.description = "This model predicts based on an SEIR model augmented with underdetection and interventions (gov response). Projections account for reopening and assume interventions would be re-enacted if cases continue to climb.";
		m3.status = "ready";
		m3.category = "Mechanistic - compartmental";
		m3.name = "CovidAnalytics-DELPHI (Differential Equations Leads to Predictions of Hospitalizations and Infections) [also known as MIT-ORC]";
		m3.type = "model";
		m3.source = "CovidAnalytics at MIT (from MIT Operations Research Center, MIT-ORC)";
		list.add(m3);
		Model m4 = new Model();
		m4.id = "4";
		m4.description = "Spatial compartment model using public mobility data and local parameters. Spatially distributed SEIR model";
		m4.status = "ready";
		m4.category = "Mechanistic - compartmental/metapopulation";
		m4.name = "JHU-APL-Bucky";
		m4.type = "dataset";
		m4.source = "John Hopkins University Applied Physics Lab";
		list.add(m4);
		Model m5 = new Model();
		m5.id = "5";
		m5.description = "Multi-layer complex network. Nodes represent people, edges are social contacts, and layers are different social activities (6 layers - home, work, transport, school, religious activities, random). States of infection are included (SIIIIRD - 7 states). Supports simulation of isolation, social distancing, pecautionary measures. In terms of population demographics, considers age distribution and family size.";
		m5.status = "processing";
		m5.category = "Mechanistic: Agent-based model (ABM)";
		m5.name = "COmplexVID-19";
		m5.type = "model";
		m5.source = "University of Sao Paulo";
		list.add(m5);
		Model m6 = new Model();
		m6.id = "6";
		m6.description =
			"""
			The simulation iterates through every member (agent) of the population once every day of the simulation. The agents will be in one of the following states throughout the epidemic: susceptible, incubation, illness, hospitalized, in ICU, dead and recovered.
			Factors such as the person's age, the contact matrix for the country, public mobility limitations, testing practices, and available healthcare capacity will have influence on how the state transitions work.
			Model is based on simulating interactions on an individual level, so arbitrary rules can be evaluated easily. Allows examining the features of the pathogen (such as contagiousness and lethality) and the features of the population (such as the number of close contacts per day) separately. Some important parameters like the reproduction number R, are emergent properties that yield their value during the simulation
			""";
		m6.status = "ready";
		m6.category = "Mechanistic: Agent-based model (ABM)";
		m6.name = "REINA";
		m6.type = "model";
		m6.source = "University of Helsinki, Finnish Institute for Health and Welfare";
		list.add(m6);
		Model m7 = new Model();
		m7.id = "7";
		m7.description = "";
		m7.status = "ready";
		m7.category = "indexed temporal";
		m7.name = "temporal_data123";
		m7.type = "dataset";
		m7.source = "University of XYZ";
		list.add(m7);
		Model m8 = new Model();
		m8.id = "8";
		m8.description = "";
		m8.status = "disabled";
		m8.category = "n/a";
		m8.name = "temp";
		m8.type = "dataset";
		m8.source = "";
		list.add(m8);
		Model m9 = new Model();
		m9.id = "9";
		m9.description = "this is a github dataset showing a lot of nice things";
		m9.status = "deprecated";
		m9.category = "weather";
		m9.name = "weather_temp_seasonal_history";
		m9.type = "dataset";
		m9.source = "";
		list.add(m9);
		Model m10 = new Model();
		m10.id = "10";
		m10.description = "population of the world at different resolution";
		m10.status = "registered";
		m10.category = "population";
		m10.name = "pop_datasetjan2022";
		m10.type = "dataset";
		m10.source = "";
		list.add(m10);

		return list;
	}
}
