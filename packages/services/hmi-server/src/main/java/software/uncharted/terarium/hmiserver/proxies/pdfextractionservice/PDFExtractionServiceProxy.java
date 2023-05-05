
package software.uncharted.terarium.hmiserver.proxies.pdfextractionservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.uncharted.terarium.hmiserver.resources.pdfextractionservice.PDFExtractionMultipartBody;
import javax.ws.rs.core.MediaType;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "pdf")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "PDF Extraction REST Endpoint")
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface PDFExtractionServiceProxy {

	@GET
	@Path("/convertpdfurl")
	Response convertPDFURL(@QueryParam("url") String url,
			@DefaultValue("pypdf2") @QueryParam("extraction_method") String extractionMethod,
			@DefaultValue("true") @QueryParam("extract_images") String extractImages);

	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	Response convertPDFTask(@MultipartForm PDFExtractionMultipartBody formData);

	@GET
	@Path("/task-result/{taskId}")
	Response getTaskStatus(@PathParam("taskId") String taskId);
}
