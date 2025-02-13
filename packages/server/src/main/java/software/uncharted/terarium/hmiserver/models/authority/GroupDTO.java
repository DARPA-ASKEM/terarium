package software.uncharted.terarium.hmiserver.models.authority;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class GroupDTO implements Serializable {

	public String name;
	public String description;
}
