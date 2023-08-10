package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Proxies TDS artifacts endpoints
 */
@RegisterRestClient(configKey = "data-service")
@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
public interface ArtifactProxy {

	@GET
	@LogRestClientTime
	List<Artifact> getArtifacts( @DefaultValue("100") @QueryParam("page_size") Integer pageSize,
															 @DefaultValue("0") @QueryParam("page") Integer page);

	@POST
	@LogRestClientTime
	Artifact createArtifact(JsonNode artifact);

	@GET
	@Path("/{id}")
	@LogRestClientTime
	Artifact getArtifact(@PathParam("id") String artifactId);

	@PUT
	@Path("/{id}")
	@LogRestClientTime
	Artifact updateArtifact(@PathParam("id") String artifactId, Artifact artifact);

	@DELETE
	@Path("/{id}")
	@LogRestClientTime
	Response deleteArtifact(@PathParam("id") String artifactId);

	@GET
	@Path("/{id}/download-url")
	@LogRestClientTime
	PresignedURL getArtifactDownloadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename);

	@GET
	@Path("/{id}/upload-url")
	@LogRestClientTime
	PresignedURL getArtifactUploadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename);
}
