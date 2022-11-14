package software.uncharted.terarium.mockdataservice.resources;

import org.jboss.resteasy.annotations.Query;
import software.uncharted.terarium.mockdataservice.models.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {


	private List<Model> getMockedModels() {
		List<Model> models = new ArrayList<>();

		models.add(new Model(
			1l,
			"CHIME",
			"Mechanistic - compartmental",
			"model",
			Instant.now(),
			"CHIME is a modified SIR model of outbreak progression that is limited to short term forecasting. It is only applicable during the period prior to a region’s peak infections, and it accounts only for a single significant social distancing policy."
		));

		models.add(new Model(
			2l,
			"University of Notre Dame CBE 30338 Class Model",
			"Mechanistic - compartmental",
			"model",
			Instant.now(),
			"Short Colab notebook in Python used to teach SIR at Notre Dame. Basic SIR/SEIR with mitigation measures like social distancing"
		));

		models.add(new Model(
			3l,
			"ovidAnalytics-DELPHI (Differential Equations Leads to Predictions of Hospitalizations and Infections) [also known as MIT-ORC]",
			"Mechanistic - compartmental",
			"model",
			Instant.now(),
			"This model predicts based on an SEIR model augmented with underdetection and interventions (gov response). Projections account for reopening and assume interventions would be re-enacted if cases continue to climb."
		));


		return models;
	}

	@GET
	public List<Model> getModels() {
		return getMockedModels();
	}

	@GET
	@Path("/{id}")
	public Model getModel(@QueryParam("id") final Long id) {
		for (Model model : getMockedModels()) {
			if (model.id == id) {
				return model;
			}
		}
		return null;
	}

	@PUT
	@Path("/{id}")
	public Model updateModel(final Long id, final Model updatedModel) {
		return updatedModel;
	}

	@DELETE
	@Path("/{id}")
	public Boolean deleteProject(final Long id) {
		return true;
	}

}
