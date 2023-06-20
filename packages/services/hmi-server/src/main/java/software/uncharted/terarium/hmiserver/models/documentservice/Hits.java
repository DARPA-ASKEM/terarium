package software.uncharted.terarium.hmiserver.models.documentservice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;

@TSModel
@Accessors(chain = true)
@Data
public class Hits implements Serializable {
	private Number value;
	private String relation;

}
