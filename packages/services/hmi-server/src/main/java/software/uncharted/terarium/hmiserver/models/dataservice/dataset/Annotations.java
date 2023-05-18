package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.List;

@Data
@Accessors(chain=true)
@TSModel
public class Annotations {

	private List<DatasetAnnotatedGeo> geo;
	private List<DatasetAnnotatedDate> date;
	private List<DatasetAnnotatedFeature> feature;

}
