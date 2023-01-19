package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDRelatedWordsResponse {
	private String status;
	private List<Object> data;
}
