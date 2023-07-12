package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@Data
@Accessors(chain = true)
public class Transition {
	private String id;
    private List<String> input;
    private List<String> output;
    @TSOptional
    private ModelGrounding grounding;
    @TSOptional
    private Properties properties;
}
