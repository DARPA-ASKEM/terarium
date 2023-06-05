package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;

@Data
@TSModel
public class DatasetAnnotations {

	@JsonAlias("data_paths")
	private List<String> dataPaths;

	private Annotations annotations;

}
