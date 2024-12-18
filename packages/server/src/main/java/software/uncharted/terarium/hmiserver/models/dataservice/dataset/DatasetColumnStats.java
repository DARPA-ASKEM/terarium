package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class DatasetColumnStats {

	private NumericColumnStats numericStats;
	private NonNumericColumnStats nonNumericStats;

	@Data
	public static class NumericColumnStats {

		private String dataType;
		private Double mean;
		private Double median;
		private Double min;
		private Double max;
		private Double stdDev;
		private List<Double> quartiles;
		private int uniqueValues;
		private int missingValues;
		private List<Double> histogramBins;
	}

	@Data
	public static class NonNumericColumnStats {

		private String dataType;
		private int uniqueValues;
		private Map<String, Long> mostCommon;
		private int missingValues;
	}
}
