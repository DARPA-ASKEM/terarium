package software.uncharted.terarium.mockdataservice.resources;

import software.uncharted.terarium.mockdataservice.models.Model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

	private final Map<Long, Model> models;

	public ModelResource() {
		this.models = new HashMap<>();

		models.put(1L, new Model(
			1L,
			"CHIME",
			"CHIME is a modified SIR model of outbreak progression that is limited to short term forecasting. It is only applicable during the period prior to a region’s peak infections, and it accounts only for a single significant social distancing policy.",
			"Mechanistic - compartmental",
			Map.of("param1", "value1", "param2", "value2")
		));

		models.put(2L, new Model(
			2L,
			"University of Notre Dame CBE 30338 Class Model",
			"Short Colab notebook in Python used to teach SIR at Notre Dame. Basic SIR/SEIR with mitigation measures like social distancing",
			"Mechanistic - compartmental",
			Map.of("param1", "value1", "param2", "value2")
		));

		models.put(3L, new Model(
			3L,
			"ovidAnalytics-DELPHI (Differential Equations Leads to Predictions of Hospitalizations and Infections) [also known as MIT-ORC]",
			"This model predicts based on an SEIR model augmented with underdetection and interventions (gov response). Projections account for reopening and assume interventions would be re-enacted if cases continue to climb.",
			"Mechanistic - compartmental",
			Map.of("param1", "value1", "param2", "value2")
		));
	}

	@GET
	public Collection<Model> getModels() {
		return this.models.values();
	}

	@GET
	@Path("/{id}")
	public Model getModel(@PathParam("id") final Long id) {
		return this.models.getOrDefault(id, null);
	}

	@PUT
	@Path("/{id}")
	public Model updateModel(@PathParam("id") final Long id, final Model updatedModel) {
		if (this.models.containsKey(updatedModel.getId())) {
			this.models.replace(updatedModel.getId(), updatedModel);
			return updatedModel;
		}
		return null;
	}

	@DELETE
	@Path("/{id}")
	public Boolean deleteProject(@PathParam("id") final Long id) {
		if (this.models.containsKey(id)) {
			this.models.remove(id);
			return true;
		}
		return false;
	}
}
