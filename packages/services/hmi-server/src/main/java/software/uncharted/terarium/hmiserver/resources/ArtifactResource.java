package software.uncharted.terarium.hmiserver.resources;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/api/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Artifacts")
@Slf4j
public class ArtifactResource {

	@Inject
	@RestClient
	ArtifactProxy artifactProxy;

	@RestClient
	JsDelivrProxy gitHubProxy;

	@GET
	public List<Artifact> getArtifacts(@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
															@DefaultValue("0") @QueryParam("page") Integer page) {
		return artifactProxy.getArtifacts(pageSize, page);
	}

	@POST
	public Artifact createArtifact(Artifact artifact) {
		return artifactProxy.createArtifact(artifact);
	}

	@GET
	@Path("/{id}")
	public Artifact getArtifact(@PathParam("id") String artifactId) {
		return artifactProxy.getArtifact(artifactId);
	}

	@PUT
	@Path("/{id}")
	public Artifact updateArtifact(@PathParam("id") String artifactId, Artifact artifact) {
		return artifactProxy.updateArtifact(artifactId, artifact);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteArtifact(@PathParam("id") String artifactId) {
		return artifactProxy.deleteArtifact(artifactId);
	}

	@GET
	@Path("/{id}/download-url")
	public PresignedURL getArtifactDownloadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {
		return artifactProxy.getArtifactDownloadUrl(artifactId, filename);
	}

	@GET
	@Path("/{id}/upload-url")
	public PresignedURL getArtifactUploadUrl(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {
		return artifactProxy.getArtifactUploadUrl(artifactId, filename);
	}

}
