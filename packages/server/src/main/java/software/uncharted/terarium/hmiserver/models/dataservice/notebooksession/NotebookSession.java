package software.uncharted.terarium.hmiserver.models.dataservice.notebooksession;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class NotebookSession extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 9176019416379347233L;

	private JsonNode data;
}
