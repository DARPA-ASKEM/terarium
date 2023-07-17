package software.uncharted.terarium.hmiserver.resources.extractionservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;


import javax.inject.Inject;
import software.uncharted.terarium.hmiserver.proxies.extractionservice.ExtractionServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
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

	@Inject
	@RestClient
	SkemaUnifiedProxy skemaUnifiedProxy;

	/**
	 * Retrieve the status of a simulation
	 *
	 * @param simulationId the id of the simulation
	 *
	 * @return the status of the simulation
	 */
	@GET
	@Path("/status/{simulation-id}")
	public Response getTaskStatus(
		@PathParam("simulation-id") final String simulationId) {
		return extractionProxy.getTaskStatus(simulationId);
	}

	/**
	 * Post MathML to skema service to get AMR return
	 *
	 * @param		framework (String) the id of the model
	 *
	 * Args:
	 *     mathMLPayload (List<String>): A list of MathML strings representing the functions that are
	 * 													         used to convert to AMR model (str, optional): AMR model return type.
	 * 													         Defaults to "petrinet". Options: "regnet", "petrinet".
	 *
	 * @return AMR model
	 */
	@POST
	@Path("/mathml-to-amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMathMLToAMR(
		@DefaultValue("petrinet") @QueryParam("framework") String framework,
		List<String> mathMLPayload
	) {
		return extractionProxy.postMathMLToAMR(framework, mathMLPayload);
	};

	/**
	 * Post LaTeX to SKEMA Unified service to get an AMR
	 * @param	framework (String) the type of AMR to return. Defaults to "petrinet". Options: "regnet", "petrinet".
	 * @param equations (List<String>): A list of LaTeX strings representing the functions that are used to convert to AMR mode.
	 * @return (Model): The AMR model
	 */
	@POST
	@Path("/latex-to-amr/{framework}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Model postLaTeXToAMR(
		@DefaultValue("petrinet") @PathParam("framework") String framework,
		List<String> equations
	) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode request = mapper.createObjectNode();
		request.put("framework", framework);
		request.set("equations", mapper.valueToTree(equations));
		return skemaUnifiedProxy.postLaTeXToAMR(request);
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
	@Path("/pdf-extractions")
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
	@Path("/profile-dataset")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postProfileDataset(
		@QueryParam("dataset_id") String datasetId,
		@QueryParam("document_text") String documentText
	) {
		return extractionProxy.postProfileDataset(datasetId, documentText);
	};
}
