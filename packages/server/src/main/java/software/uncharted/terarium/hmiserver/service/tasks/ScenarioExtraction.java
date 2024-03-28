package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.utils.GreekDictionary;

import java.util.List;

public class ScenarioExtraction {
	public static List<ModelParameter> getModelParameters(JsonNode condition, Model modelCopy) {
		final List<ModelParameter> modelParameters = modelCopy.getParameters();
		modelParameters.forEach((parameter) -> {
			final String parameterId = parameter.getId();
			final JsonNode conditionParameters = condition.get("parameters");
			conditionParameters.forEach((conditionParameter) -> {
				// Get the parameter value from the condition
				final String id = conditionParameter.get("id").asText();

				// Test against the id of the parameter in greek alphabet or english
				if (parameterId.equals(id) || parameterId.equals(GreekDictionary.englishToGreek(id))) {
					parameter.setValue(conditionParameter.get("value").doubleValue());
				}
			});
		});
		return modelParameters;
	}
}
