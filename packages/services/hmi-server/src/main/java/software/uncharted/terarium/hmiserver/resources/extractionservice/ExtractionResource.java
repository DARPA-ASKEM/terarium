package software.uncharted.terarium.hmiserver.resources.extractionservice;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import software.uncharted.terarium.hmiserver.proxies.extractionservice.ExtractionServiceProxy;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/extract")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public class ExtractionResource {

	@Inject
	@RestClient
	ExtractionServiceProxy extractionProxy;

	/**
	 * Retrieve the status of a simulation
	 *
	 * @param simulationId the id of the simulation
	 *
	 * @return the status of the simulation
	 */
	@GET
	@Path("/status/{simulation_id}")
	public Response getTaskStatus(
		@PathParam("simulation_id") final String simulationId) {
		return extractionProxy.getTaskStatus(simulationId);
	}

	/**
	 * Post MathML to skema service to get AMR return
	 *
	 * @param		model (String) the id of the model
	 *
	 * Args:
	 *     mathMLPayload (List<String>): A list of MathML strings representing the functions that are
	 * 													         used to convert to AMR model (str, optional): AMR model return type.
	 * 													         Defaults to "petrinet". Options: "regnet", "petrinet".
	 *
	 * @return AMR model
	 */
	@POST
	@Path("/mathml_to_amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMathMLToAMR(
		@DefaultValue("petrinet") @QueryParam("framework") String framework,
		List<String> mathMLPayload
	) {
		return extractionProxy.postMathMLToAMR(framework, mathMLPayload);
	};

	/**
	 * Post a PDF to the extraction service
	 *
	 * @param    annotateSkema (Boolean): Whether to annotate the PDF with Skema
	 * @param    annotateMIT (Boolean): Whether to annotate the PDF with AMR
	 * @param    name (String): The name of the PDF
	 * @param    description (String): The description of the PDF
	 *
	 * Args:
	 *     pdf (Object): The PDF file to upload
	 *
	 * @return extractions of the pdf
	 */
	@POST
	@Path("/pdf_extractions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postPDFExtractions(
		@DefaultValue("true") @QueryParam("annotate_skema") Boolean annotateSkema,
		@DefaultValue("true") @QueryParam("annotate_amr") Boolean annotateMIT,
		@QueryParam("name") String name,
		@QueryParam("model") String description,
		Object pdf
	) {
		return extractionProxy.postPDFExtractions(annotateSkema, annotateMIT, name, description, pdf);
	};

	/**
	 * Profile a dataset
	 *
	 * @param		datasetId (String): The ID of the dataset to profile
	 * @param		documentText (String): The text of the document to profile
	 *
	 * @return the profiled dataset
	 */
	@POST
	@Path("/profile_dataset")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postProfileDataset(
		@QueryParam("dataset_id") String datasetId,
		@QueryParam("document_text") String documentText
	) {
		return extractionProxy.postProfileDataset(datasetId, documentText);
	};
}
