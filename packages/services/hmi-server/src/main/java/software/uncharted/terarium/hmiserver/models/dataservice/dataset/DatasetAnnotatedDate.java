package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain=true)
@TSModel
public class DatasetAnnotatedDate extends DatasetAnnotatedField{
	@JsonAlias("date_type")
	private String dateType;

	@JsonAlias("primary_date")
	private Boolean primaryDate;

	@JsonAlias("time_format")
	private String timeFormat;

}
