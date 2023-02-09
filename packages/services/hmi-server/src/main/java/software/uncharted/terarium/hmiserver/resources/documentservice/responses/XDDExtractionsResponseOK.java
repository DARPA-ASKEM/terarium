package software.uncharted.terarium.hmiserver.resources.documentservice.responses;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;


import java.util.List;

@Data
@Accessors(chain = true)
public class XDDExtractionsResponseOK extends XDDResponseOK {
	private List<Extraction> data;

	private Number total;

	private Number page;
}
