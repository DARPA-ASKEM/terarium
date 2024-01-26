package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class Position implements Serializable {

	private Number x;
	private Number y;
}
