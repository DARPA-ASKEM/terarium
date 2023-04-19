package software.uncharted.terarium.hmiserver.models.dataservice;

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
public class CsvAsset implements Serializable {
	List<List<String>> csv;
	@TSOptional
	List<CsvColumnStats> stats;
	List<String> headers;

	public CsvAsset(List<List<String>> csv, List<CsvColumnStats> stats, List<String> headers){
		this.csv = csv;
		this.stats = stats;
		this.headers = headers;
	}

	public CsvAsset(List<List<String>> csv, List<String> headers){
		this.csv = csv;
		this.stats = null;
		this.headers = headers;
	}
}
