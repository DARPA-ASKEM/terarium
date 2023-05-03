
package software.uncharted.terarium.hmiserver.proxies.pdfextractionservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.uncharted.terarium.hmiserver.resources.pdfextractionservice.PDFExtractionMultipartBody;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "pdf")
@Produces(MediaType.APPLICATION_JSON)
@Tag(	name = "PDF Extraction REST Endpoint")
public interface PDFExtractionServiceProxy {

	@GET
	@Path("/convertpdfurl")
	Response convertPdfUrl(@QueryParam("url") String url,
			@DefaultValue("pypdf2") @QueryParam("extraction_method") String extractionMethod,
			@DefaultValue("true") @QueryParam("extract_images") String extractImages);

	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	Response convertPDFTask(@MultipartForm PDFExtractionMultipartBody formData);

	@GET
	@Path("/task_result/{taskId}")
	Response getTaskStatus(@PathParam("taskId") String taskId);
}
