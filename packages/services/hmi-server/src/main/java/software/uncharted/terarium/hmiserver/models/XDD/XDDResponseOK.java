package software.uncharted.terarium.hmiserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class XDDResponseOK implements Serializable {
	private Number v;

	private String license;
};
