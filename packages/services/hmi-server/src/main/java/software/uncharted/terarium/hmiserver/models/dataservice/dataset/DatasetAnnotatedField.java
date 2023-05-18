package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;

@Data
@Accessors(chain=true)
@TSModel
public abstract class DatasetAnnotatedField {
	private String name;
	@JsonAlias("display_name")
	private String displayName;

	private String description;

	private String type;

	private List<String> qualifies;

	private Object aliases;
}
