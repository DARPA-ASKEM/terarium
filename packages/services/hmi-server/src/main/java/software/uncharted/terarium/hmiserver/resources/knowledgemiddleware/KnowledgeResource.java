package software.uncharted.terarium.hmiserver.resources.knowledgemiddleware;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.extractionservice.ExtractionResponse;
import software.uncharted.terarium.hmiserver.proxies.knowledgemiddleware.KnowledgeMiddlewareProxy;
import software.uncharted.terarium.hmiserver.proxies.skema.SkemaUnifiedProxy;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/knowledge")
@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public class KnowledgeResource {

	@Inject
	@RestClient
	KnowledgeMiddlewareProxy knowledgeMiddlewareProxy;

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
		return knowledgeMiddlewareProxy.getTaskStatus(id);
	}

	/**
	 * Post LaTeX to SKEMA Unified service to get an AMR
	 * @param   framework (String) the type of AMR to return. Options: "regnet", "petrinet".
	 * @param   modelId (String): the id of the model (to update) based on the set of equations
	 * @param   equations (List<String>): A list of LaTeX strings representing the functions that are used to convert to AMR model
	 * @return  (ExtractionResponse): The response from the extraction service
	 */
	@POST
	@Path("/mathml-to-amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public ExtractionResponse postMathMLToAMR(
			@QueryParam("framework") String framework,
			@QueryParam("modelId") String modelId,
			List<String> equations
	) {
		// http://knowledge-middleware.staging.terarium.ai/#/default/equations_to_amr_equations_to_amr_post
		return knowledgeMiddlewareProxy.postEquationsToAMR("mathml", framework, modelId, equations);
	};

	/**
	 * Post LaTeX to SKEMA Unified service to get an AMR
	 * @param   framework (String) the type of AMR to return. Options: "regnet", "petrinet".
	 * @param   modelId (String): the id of the model (to update) based on the set of equations
	 * @param   equations (List<String>): A list of LaTeX strings representing the functions that are used to convert to AMR model
	 * @return  (ExtractionResponse): The response from the extraction service
	 */
	@POST
	@Path("/latex-to-amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public ExtractionResponse postLaTeXToAMR(
		@QueryParam("framework") String framework,
		@QueryParam("modelId") String modelId,
		List<String> equations
	) {
		// http://knowledge-middleware.staging.terarium.ai/#/default/equations_to_amr_equations_to_amr_post
		return knowledgeMiddlewareProxy.postEquationsToAMR("latex", framework, modelId, equations);
	};

	/**
	 * Transform source code to AMR
	 * @param 	codeId (String): id of the code artifact
	 * @param 	name (String): the name to set on the newly created model
	 * @param 	description (String): the description to set on the newly created model
	 * @return  (ExtractionResponse)
	 */
	@POST
	@Path("/code-to-amr")
	@Consumes(MediaType.APPLICATION_JSON)
	public ExtractionResponse postCodeToAMR(
		@QueryParam("code_id") String codeId,
		@QueryParam("name") String name,
		@QueryParam("description") String description
	) {
		return knowledgeMiddlewareProxy.postCodeToAMR(codeId, name, description);
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
		return knowledgeMiddlewareProxy.postPDFExtractions(artifactId, annotateSkema, annotateMIT, name, description);
	};

	/**
	 * Post a PDF to the extraction service to get text
	 * @param artifactId (String): The ID of the artifact to extract text from
	 * @return response status of queueing this operation
	 */
	@POST
	@Path("/pdf-to-text")
	public Response postPDFToText(@QueryParam("artifact_id") String artifactId){
		return knowledgeMiddlewareProxy.postPDFToText(artifactId);
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
		return knowledgeMiddlewareProxy.postProfileModel(modelId, artifactId);
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
		return knowledgeMiddlewareProxy.postProfileDataset(datasetId, artifactId);
	};


}
