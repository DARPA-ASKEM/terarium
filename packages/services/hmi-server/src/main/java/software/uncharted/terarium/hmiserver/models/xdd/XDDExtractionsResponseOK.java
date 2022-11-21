package software.uncharted.terarium.hmiserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDExtractionsResponseOK extends XDDResponseOK {
	private List<Extraction> data;

	private Number total;

	private Number page;
}
