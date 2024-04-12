package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Transform implements Serializable {

	Number x;
	Number y;
	Number k;
}
