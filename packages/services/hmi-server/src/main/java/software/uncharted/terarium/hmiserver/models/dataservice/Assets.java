package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@Slf4j
public class Assets extends HashMap<String, List<Map<String, Object>>> implements Serializable {


	public List<Publication> getPublications() {
		return getResourceType(Publication.class, ResourceType.Type.PUBLICATIONS);
	}

	public List<Model> getModels() {
		return getResourceType(Model.class, ResourceType.Type.MODELS);
	}

	public List<SimulationPlan> getPlans() {
		return getResourceType(SimulationPlan.class, ResourceType.Type.PLANS);
	}

	public List<SimulationRun> getRuns() {
		return getResourceType(SimulationRun.class, ResourceType.Type.SIMULATION_RUNS);
	}

	public List<Dataset> getDatasets() {
		return getResourceType(Dataset.class, ResourceType.Type.DATASETS);
	}

	// TODO: there is no "Extraction" class
	// public List<SimulationRun> getExtractions() {
	// 	return getResourceType(Extraction.class, ResourceType.Type.EXTRACTIONS);
	// }

	/**
	 * Return the values of the resourceType requested
	 *
	 * @param clazz Class of the resourceType
	 * @param type
	 * @param <T>
	 * @return
	 */
	public <T extends ResourceType> List<T> getResourceType(Class<T> clazz, ResourceType.Type type) {
		if (get(type.type).isEmpty())
			return Collections.emptyList();

		Jsonb jsonb = JsonbBuilder.create();

		List<T> resources = new ArrayList<>(get(type.type).size());
		for (Map<String, Object> m : get(type.type)) {
			try {
				resources.add(jsonb.fromJson(new ObjectMapper().writeValueAsString(m), clazz));
			} catch (JsonProcessingException e) {
				log.warn("Unable to parse " + clazz.getName() + " from HashMap", e);
			}
		}

		return resources;
	}
}
