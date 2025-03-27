package software.uncharted.terarium.hmiserver.models.extraction;

import lombok.Data;

@Data
class PageSize {

	private Double width;
	private Double height;
}

@Data
public class ExtractionPage {

	private Integer page;
	private PageSize size;
}
