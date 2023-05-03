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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Path("/api/extract")
@Slf4j
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class PDFExtractionResource {

	@Inject
	@RestClient
	PDFExtractionServiceProxy extractionProxy;

	//
	// takes a URL of a PDF, downloads it and then extracts the text. Returns a
	// task_id
	@GET
	@Path("/convertpdfurl")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response convertPdfUrl(@QueryParam("url") String url,
			@QueryParam("extraction_method") String extractionMethod,
			@QueryParam("extract_images") String extractImages) {
		return extractionProxy.convertPdfUrl(url, extractionMethod, extractImages);
	}

	//
	// takes a pdf file and extracts the text.
	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response convertPDFTask(@MultipartForm PDFExtractionMultipartBody formData) throws Exception {
		log.error("THIS IS GOOD");
		log.error(formData.fileName);

		PDFExtractionMultipartBody body = new PDFExtractionMultipartBody();
		body.fileName = "greeting.txt";
		body.file = new ByteArrayInputStream("HELLO WORLD".getBytes(StandardCharsets.UTF_8));
		return extractionProxy.convertPDFTask(body);

	}

	// checks a taskId status
	@GET
	@Path("/task_result/{taskId}")
	public Response getTaskStatus(
			@PathParam("taskId") final String taskId) {
		return extractionProxy.getTaskStatus(taskId);
	}
}
