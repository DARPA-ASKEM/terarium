package software.uncharted.terarium.hmiserver.resources.pdfextractionservice;

import javax.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import javax.ws.rs.core.MediaType;

public class PDFExtractionUrlToPDFForm {

	@FormParam("url")
	@PartType(MediaType.TEXT_PLAIN)
	public String url;

	@FormParam("extraction_method")
	@PartType(MediaType.TEXT_PLAIN)
	public String extractionMethod;

	@FormParam("extract_images")
	@PartType(MediaType.TEXT_PLAIN)
	public String extractImages;
}
