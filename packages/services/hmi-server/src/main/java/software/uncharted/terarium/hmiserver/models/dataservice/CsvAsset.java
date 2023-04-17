package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@Slf4j
public class CsvAsset implements Serializable {
	List<List<String>> csv;
	List<List<Integer>> bins;
	List<String> headers;

	public CsvAsset(List<List<String>> csv, List<List<Integer>> bins, List<String> headers){
		this.csv = csv;
		this.bins = bins;
		this.headers = headers;
	}

	public CsvAsset(List<List<String>> csv, List<String> headers){
		this.csv = csv;
		this.bins = null;
		this.headers = headers;
	}

}
