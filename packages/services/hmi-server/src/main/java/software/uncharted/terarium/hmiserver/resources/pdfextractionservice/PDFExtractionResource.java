package software.uncharted.terarium.hmiserver.resources.pdfextractionservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import software.uncharted.terarium.hmiserver.proxies.pdfextractionservice.PDFExtractionServiceProxy;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/extract")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public class PDFExtractionResource {

	@Inject
	@RestClient
	PDFExtractionServiceProxy extractionProxy;

	// takes a URL of a PDF, downloads it and then extracts the text. Returns a
	// task_id
	@GET
	@Path("/convertpdfurl")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response convertPdfUrl(@QueryParam("url") String url,
			@QueryParam("extraction_method") String extractionMethod,
			@QueryParam("extract_images") String extractImages) {
		return extractionProxy.convertPDFURL(url, extractionMethod, extractImages);
	}

	// takes a pdf file and extracts the text.
	@POST
	@Path("/convertpdftask")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response convertPDFTask(@FormDataParam("file") InputStream file,
			@DefaultValue("pymupdf") @QueryParam("extraction_method") String extractionMethod,
			@DefaultValue("true") @QueryParam("extract_images") String extractImages)
			throws IOException {

		MultipartFormDataOutput pdfExtractionData = new MultipartFormDataOutput();
		pdfExtractionData.addFormData("file", file.readAllBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE, "foo.pdf");
		try {
			return extractionProxy.convertPDFTask(pdfExtractionData, extractionMethod, extractImages);
		} catch (HmiResponseExceptionMapper.ResponseRuntimeException e) {
			log.error(e.toString());
			if (e.getStatus() == 404) {
				return Response.noContent().build();
			} else {
				return Response
						.status(Response.Status.INTERNAL_SERVER_ERROR)
						.type(MediaType.APPLICATION_JSON)
						.build();
			}
		}
	}

	// checks a taskId status
	@GET
	@Path("/task-result/{taskId}")
	public Response getTaskStatus(
			@PathParam("taskId") final String taskId) {
		return extractionProxy.getTaskStatus(taskId);
	}
}
