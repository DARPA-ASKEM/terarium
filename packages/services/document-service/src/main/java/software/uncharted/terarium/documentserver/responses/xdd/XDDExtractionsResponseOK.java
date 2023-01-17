package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.documentserver.models.xdd.Extraction;

import java.util.List;

@Data
@Accessors(chain = true)
public class XDDExtractionsResponseOK extends XDDResponseOK {
	private List<Extraction> data;

	private Number total;

	private Number page;
}
