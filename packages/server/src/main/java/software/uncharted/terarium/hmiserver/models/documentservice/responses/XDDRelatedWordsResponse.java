package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDRelatedWordsResponse {
	private String status;
	private List<Object> data;
}
