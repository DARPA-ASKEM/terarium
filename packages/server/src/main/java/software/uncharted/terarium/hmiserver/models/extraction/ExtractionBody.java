package software.uncharted.terarium.hmiserver.models.extraction;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
public class ExtractionBody {

	private String id;
	private List<ExtractionRef> children;
}
