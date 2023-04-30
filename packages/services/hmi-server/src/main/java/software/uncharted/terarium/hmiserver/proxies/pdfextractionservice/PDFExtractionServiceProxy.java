
package software.uncharted.terarium.hmiserver.proxies.pdfextractionservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.uncharted.terarium.hmiserver.resources.pdfextractionservice.PDFExtractionMultipartBody;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "pdf")
@Produces(MediaType.APPLICATION_JSON)
public interface PDFExtractionServiceProxy {

	@POST
	@Path("/convertpdfurl")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	Response convertPdfUrl(@FormParam("url") String url,
			@FormParam("extraction_method") String extractionMethod,
			@FormParam("extract_images") String extractImages);

	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	Response convertPDFTask(@MultipartForm PDFExtractionMultipartBody formData);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/task_result/{taskId}")
	Response getTaskStatus(@PathParam("taskId") String taskId);
}
