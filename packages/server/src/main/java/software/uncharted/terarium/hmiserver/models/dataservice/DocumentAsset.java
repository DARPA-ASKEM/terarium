package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class DocumentAsset implements Serializable {

	@TSOptional
	private Long id;

	@JsonProperty("xdd_uri")
	private String xddUri;

	private String title;

}
