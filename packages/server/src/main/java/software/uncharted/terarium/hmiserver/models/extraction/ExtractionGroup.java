package software.uncharted.terarium.hmiserver.models.extraction;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class ExtractionGroup {

	private String id;
	private List<ExtractionRef> children;
}
