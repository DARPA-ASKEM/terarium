package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Proxies TDS artifacts endpoints
 */
@RegisterRestClient(configKey = "data-service")
@Path("/artifacts")
@Produces(MediaType.APPLICATION_JSON)
public interface ArtifactProxy {

	@GET
	List<Artifact> getArtifacts( @DefaultValue("100") @QueryParam("page_size") Integer pageSize,
															 @DefaultValue("0") @QueryParam("page") Integer page);

	@POST
	Artifact createArtifact(Artifact artifact);

	@GET
	@Path("/{id}")
	Artifact getArtifact(@PathParam("id") String artifactId);

	@PUT
	@Path("/{id}")
	Artifact updateArtifact(@PathParam("id") String artifactId, Artifact artifact);

	@DELETE
	@Path("/{id}")
	Response deleteArtifact(@PathParam("id") String artifactId);

	@GET
	@Path("/{id}/download-url")
	PresignedURL getArtifactDownloadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename);

	@GET
	@Path("/{id}/upload-url")
	PresignedURL getArtifactUploadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename);

}
