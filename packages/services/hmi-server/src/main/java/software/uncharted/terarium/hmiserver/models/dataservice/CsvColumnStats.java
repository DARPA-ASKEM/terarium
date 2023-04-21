package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@Slf4j
@TSModel
public class CsvColumnStats implements Serializable {
	List<Integer> bins; 
	double minValue; 
	double maxValue;
	double mean;
	double median;
	double sd;

	public CsvColumnStats(List<Integer> bins, double minValue, double maxValue, double mean, double median, double sd){
		this.bins = bins;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.mean = mean;
		this.median = median;
		this.sd = sd;
	}
}
