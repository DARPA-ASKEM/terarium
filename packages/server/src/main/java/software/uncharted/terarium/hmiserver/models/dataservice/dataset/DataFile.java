package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

/**
 * Represents a dataset source file.
 */
@Data
@Accessors(chain = true)
@TSModel
public abstract class DataFile {

	String name;

	String url;

	abstract boolean hasColumns();

	abstract List<DatasetColumn> getColumns();

}
