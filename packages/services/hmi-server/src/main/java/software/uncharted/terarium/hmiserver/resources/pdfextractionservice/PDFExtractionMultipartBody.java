package software.uncharted.terarium.hmiserver.resources.pdfextractionservice;

import java.io.InputStream;
import javax.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import javax.ws.rs.core.MediaType;

public class PDFExtractionMultipartBody {

	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;

}
