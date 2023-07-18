
package software.uncharted.terarium.hmiserver.proxies.extractionservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.ws.rs.core.MediaType;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "extraction-service-api")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Extraction Service")
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface ExtractionServiceProxy {

	/**
	 * Retrieve the status of a simulation
	 *
	 * @param simulationId the id of the simulation
	 *
	 * @return the status of the simulation
	 */
	@GET
	@Path("/status/{simulation_id}")
	Response getTaskStatus(
		@PathParam("simulation_id") String simulationId
	);

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
	Response postMathMLToAMR(
		@DefaultValue("petrinet") @QueryParam("model") String model,
		List<String> mathMLPayload
	);

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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	Response postPDFExtractions(
		@DefaultValue("true") @QueryParam("annotate_skema") Boolean annotateSkema,
		@DefaultValue("true") @QueryParam("annotate_amr") Boolean annotateMIT,
		@QueryParam("name") String name,
		@QueryParam("description") String description,
		Object pdf
	);

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
	Response postProfileDataset(
		@QueryParam("dataset_id") String datasetId,
		@QueryParam("document_text") String documentText
	);
}
