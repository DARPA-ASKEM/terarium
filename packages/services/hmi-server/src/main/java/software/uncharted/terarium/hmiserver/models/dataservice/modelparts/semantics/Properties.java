package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.VariableStatement;

@Data
@Accessors(chain = true)
public class Properties {
	private String name;
    
    @TSOptional
    private ModelGrounding grounding;
}
