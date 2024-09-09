package software.uncharted.terarium.hmiserver.models.climateData;

import java.util.List;
import lombok.Data;

@Data
public class ClimateDataResultPreviews {

	private List<Preview> previews;

	@Data
	public static class Preview {

		String image;
		String year;
	}
}
