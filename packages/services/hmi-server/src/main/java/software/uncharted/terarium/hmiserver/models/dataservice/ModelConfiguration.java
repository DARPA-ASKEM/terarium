package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonAlias;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;

@Data
@Accessors(chain = true)
@TSModel
public class ModelConfiguration {
		private String id;

    private String name;

    @TSOptional
    private String description;

		@JsonAlias("model_id")
    private String modelId;

    private Object configuration;

		@JsonAlias("amr_configuration")
    private Object amrConfiguration;
}
