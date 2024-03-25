package software.uncharted.terarium.hmiserver.models.extractionservice;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PdfExtractionRequest {
	private final Boolean compress_images;
	private final Boolean use_cache;
}
