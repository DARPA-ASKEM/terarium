package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDResponseOK implements Serializable {
	/**
	 * Version
	 **/
	private Number v;

	private String license;
}
