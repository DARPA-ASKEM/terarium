package software.uncharted.terarium.hmiserver.models.climateData;

import lombok.Data;

import java.util.List;

@Data
public class ClimateDataResultPreviews {
    private List<Preview> previews;

    @Data
    public static class Preview {
        String image;
        String year;
    }
}
