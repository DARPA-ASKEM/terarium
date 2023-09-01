package software.uncharted.terarium.hmiserver.model.dataservice;

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

	public CsvAsset(List<List<String>> csv, List<CsvColumnStats> stats, List<String> headers, Integer rowCount) {
		this.csv = csv;
		this.stats = stats;
		this.headers = headers;
		this.rowCount = rowCount;
		this.data = new ArrayList<>();
		for(int i = 1; i < csv.size(); i++){
			Map<String,String> row = new HashMap<>();
			for(int j = 0; j < headers.size(); j++){
				row.put(headers.get(j), csv.get(i).get(j));
			}
			data.add(row);
		}



	}


	/** The csv data. Note that this may be incomplete if the dataset is too large. **/
	List<List<String>> csv;

	/** Stats about the entire CSV file, not just the contents represented here **/
	@TSOptional
	List<CsvColumnStats> stats;

	/** Headers on this CSV file **/
	List<String> headers;

	/** The number of rows in the CSV file. This may be a larger value than the csv object contained within **/
	Integer rowCount;

	/** The CSV represented in a map form, where each entry in the list is a row. Column names are keys **/
	List<Map<String,String>> data;

}
