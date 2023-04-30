package software.uncharted.terarium.hmiserver.resources.pdfextractionservice;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import javax.inject.Inject;
import io.quarkus.security.Authenticated;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.uncharted.terarium.hmiserver.proxies.pdfextractionservice.PDFExtractionServiceProxy;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/extract")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
public class PDFExtractionResource {

	@Inject
	@RestClient
	PDFExtractionServiceProxy extractionProxy;

	// takes a URL of a PDF, downloads it and then extracts the text.  Returns a task_id
	@POST
	@Path("/convertpdfurl")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response convertPdfUrl(@MultipartForm PDFExtractionUrlToPDFForm formData) {
		return extractionProxy.convertPdfUrl(formData.url, formData.extractionMethod, formData.extractImages);
	}


	// takes a pdf file and extracts the text.  Returns a taskId TODO: Make this work :P
	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response convertPDFTask(@MultipartForm PDFExtractionMultipartBody formData) throws Exception {
		System.out.println("THIS IS GOOD");
		return extractionProxy.convertPDFTask(formData);// , extractionMethod, extractImages);
	}

	// checks a taskId status
	@GET
	@Path("/task_result/{taskId}")
	public Response getModelJSON(
			@PathParam("taskId") final String taskId) {
		return extractionProxy.getTaskStatus(taskId);
	}

}
