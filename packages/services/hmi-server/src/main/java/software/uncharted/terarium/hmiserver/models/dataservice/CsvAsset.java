package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@Slf4j
@TSModel
@AllArgsConstructor
public class CsvAsset implements Serializable {

	/** The csv data. Note that this may be incomplete if the dataset is too large. **/
	List<List<String>> csv;

	/** Stats about the entire CSV file, not just the contents represented here **/
	@TSOptional
	List<CsvColumnStats> stats;

	/** Headers on this CSV file **/
	List<String> headers;

	/** The number of rows in the CSV file. This may be a larger value than the csv object contained within **/
	Integer rowCount;

}
