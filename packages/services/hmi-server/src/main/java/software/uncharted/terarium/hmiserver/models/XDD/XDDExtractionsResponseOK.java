package software.uncharted.terarium.hmiserver.models.xdd;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class XDDExtractionsResponseOK extends XDDResponseOK {
	private List<Extraction> data;

	private Number total;

	private Number page;
};
