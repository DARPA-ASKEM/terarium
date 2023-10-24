package software.uncharted.terarium.hmiserver.models.funman.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.funman.models.FunmanBoxTypes;
import software.uncharted.terarium.hmiserver.models.funman.models.FunmanPointType;

import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class ParameterSpace {
		@JsonProperty("true_points")
		private List<FunmanPointType> truePoints;

		@JsonProperty("false_points")
		private List<FunmanPointType> falsePoints;

		@JsonProperty("true_boxes")
		private List<FunmanBoxTypes> trueBoxes;

		@JsonProperty("false_boxes")
		private List<FunmanBoxTypes> falseBoxes;
}