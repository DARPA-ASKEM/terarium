package software.uncharted.terarium.hmiserver.resources.extractionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.extractionservice.ExtractionServiceProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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

	@Inject
	@RestClient
	ArtifactProxy artifactProxy;

	/**
	 * Retrieve the status of an extraction job
	 * @param id (String) the id of the extraction job
	 * @return the status of the extraction job
	 */
	@GET
	@Path("/status/{id}")
	public Response getTaskStatus(
		@PathParam("id") final String id) {
		return extractionProxy.getTaskStatus(id);
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
		/* Create the JSON request containing the LaTeX equations and model framework:
		 * https://skema-unified.staging.terarium.ai/docs#/workflows/equations_to_amr_workflows_latex_equations_to_amr_post
		 * ie: { "equations": [ "equation1", "equation2", ... ], "model": "petrinet" }
		 */
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode request = mapper.createObjectNode();
		request.put("model", framework);
		request.set("equations", mapper.valueToTree(equations));
		return skemaUnifiedProxy.postLaTeXToAMR(request);
	};

	/**
	 * Transform source code to AMR
	 * @param 	artifactId (String): id of the code artifact
	 * @param 	name (String): the name to set on the newly created model
	 * @param 	description (String): the description to set on the newly created model
	 * @return  (ExtractionResponse)
	 */
	@POST
	@Path("/code-to-amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public ExtractionResponse postCodeToAMR(
		String artifactId,
		String name,
		String description
	) {
		// Fetch the related artifact to fill potential missing name and description
		final Artifact artifact = artifactProxy.getArtifact(artifactId);
		if (name == null) {	name = artifact.getName(); }
		if (description == null) { description = artifact.getDescription();	}

		return extractionProxy.postCodeToAMR(artifactId, name, description);
	}


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
	 * @return response status of queueing this operation
	 */
	@POST
	@Path("/pdf-extractions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postPDFExtractions(
		@QueryParam("artifact_id") String artifactId,
		@DefaultValue("true") @QueryParam("annotate_skema") Boolean annotateSkema,
		@DefaultValue("true") @QueryParam("annotate_mit") Boolean annotateMIT,
		@QueryParam("name") String name,
		@QueryParam("description") String description
	) {
		return extractionProxy.postPDFExtractions(artifactId, annotateSkema, annotateMIT, name, description);
	};

	/**
	 * Post a PDF to the extraction service to get text
	 * @param artifactId (String): The ID of the artifact to extract text from
	 * @return response status of queueing this operation
	 */
	@POST
	@Path("/pdf-to-text")
	public Response postPDFToText(@QueryParam("artifact_id") String artifactId){
		return extractionProxy.postPDFToText(artifactId);
	}

	/**
	 * Profile a model
	 *
	 * @param		modelId (String): The ID of the model to profile
	 * @param		documentText (String): The text of the document to profile
	 *
	 * @return the profiled model
	 */
	@POST
	@Path("/profile-model/{model_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postProfileModel(
		@PathParam("model_id") String modelId,
		@QueryParam("artifact_id") String artifactId
	) {
		return extractionProxy.postProfileModel(modelId, artifactId);
	};

	/**
	 * Profile a dataset
	 *
	 * @param		datasetId (String): The ID of the dataset to profile
	 * @param		artifactId (String): The ID of the artifact to profile
	 *
	 * @return the profiled dataset
	 */
	@POST
	@Path("/profile-dataset/{dataset_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postProfileDataset(
		@PathParam("dataset_id") String datasetId,
		@QueryParam("artifact_id") String artifactId
	) {
		return extractionProxy.postProfileDataset(datasetId, artifactId);
	};
}
