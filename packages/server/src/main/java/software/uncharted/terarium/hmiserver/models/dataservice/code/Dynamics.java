package software.uncharted.terarium.hmiserver.models.dataservice.code;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Dynamics {

	private String name;
	private String description;
	private List<String> block;

	@Override
	public Dynamics clone() {
		final Dynamics clone = new Dynamics();
		clone.setName(name);
		clone.setDescription(description);
		if (this.block != null) clone.setBlock(new ArrayList<>(block));
		return clone;
	}
}
