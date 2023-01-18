package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.documentserver.models.xdd.Document;

@Data
@Accessors(chain = true)
public class XDDResponseOK {

	/**
	 * The related document
	 **/
	private Document bibjson;

	private Number score;

}
