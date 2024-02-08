package software.uncharted.terarium.hmiserver.models.dataservice.notebooksession;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class NotebookSession extends TerariumAsset  {


	private String name;

	@TSOptional
	private String description;

	private JsonNode data;

}
