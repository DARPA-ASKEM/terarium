package software.uncharted.terarium.hmiserver.resources.dataservice;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.resources.DataStorageResource;
import software.uncharted.terarium.hmiserver.resources.SnakeCaseResource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/api/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Artifacts")
@Slf4j
public class ArtifactResource extends DataStorageResource implements SnakeCaseResource {

	@Inject
	@RestClient
	ArtifactProxy artifactProxy;

	@RestClient
	@Inject
	JsDelivrProxy gitHubProxy;

	@ConfigProperty(name = "aws.artifact_path")
	Optional<String> artifactPath;





	@GET
	public List<Artifact> getArtifacts(@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
															@DefaultValue("0") @QueryParam("page") Integer page) {
		return artifactProxy.getArtifacts(pageSize, page);
	}

	@PUT
	public Artifact createArtifact(Artifact artifact) {
		return artifactProxy.createArtifact(convertObjectToSnakeCaseJsonNode(artifact));
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

	@GET
	@Path("/{id}/download-file-as-text")
	public Response downloadFileAsText(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {

		log.debug("Downloading artifact {} from project", artifactId);

		if (artifactPath.isEmpty()) {
			log.error("Artifact path not set");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		String objectKey = String.format("%s/%s/%s", artifactPath.get(), artifactId, filename);

		String fileAsString =  downloadStringFromS3(objectKey);

		return Response.status(Response.Status.OK).entity(fileAsString).type(MediaType.TEXT_PLAIN).build();

	}

	@GET
	@Path("/{id}/download-file")
	public Response downloadFile(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {

		log.debug("Downloading artifact {} from project", artifactId);

		if (artifactPath.isEmpty()) {
			log.error("Artifact path not set");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		String objectKey = String.format("%s/%s/%s", artifactPath.get(), artifactId, filename);

		byte[] fileAsBytes =  downloadBytesFromS3(objectKey);

		return Response.status(Response.Status.OK).entity(fileAsBytes).build();

	}

	@PUT
	@Path("/{artifactId}/uploadFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
		@PathParam("artifactId") final String artifactId,
		@QueryParam("filename") final String filename,
		Map<String, InputStream> input
	) throws IOException {

		log.debug("Uploading artifact {} to project", artifactId);


		if (artifactPath.isEmpty()) {
			log.error("Artifact path not set");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		String objectKey = String.format("%s/%s/%s", artifactPath.get(), artifactId, filename);

		SdkHttpResponse res = uploadBytesToS3(objectKey, input.get("file").readAllBytes());

		//find the status of the response
		if (res != null && res.isSuccessful()) {
			log.debug("Successfully uploaded file file to dataset {}", artifactId);
			return Response
				.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON)
				.build();
		} else {
			log.error("Failed to upload file file to dataset {}", artifactId);
			if(res == null  || res.statusText().isEmpty()) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
					.build();
			}
			return Response.status(res.statusCode(), res.statusText().get()).type(MediaType.APPLICATION_JSON)
				.build();
		}


	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 */
	@PUT
	@Path("/{artifactId}/uploadArtifactFromGithub")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response uploadArtifactFromGithub(
		@PathParam("artifactId") final String artifactId,
		@QueryParam("path") final String path,
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("filename") final String filename
	){
		log.debug("Uploading artifact file from github to dataset {}", artifactId);

		//verify that dataSetPath and bucket are set. If not, return an error
		if (artifactPath.isEmpty()) {
			log.error("artifactPath information not set. Cannot upload file from github.");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		//download file from GitHub
		String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path);

		String objectKey = String.format("%s/%s/%s", artifactPath.get(), artifactId, filename);


		SdkHttpResponse res = uploadStringToS3(objectKey, fileString);

		//find the status of the response
		if (res.isSuccessful()) {
			log.debug("Successfully uploaded artifact file to dataset {}", artifactId);
			return Response
				.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON)
				.build();
		} else {
			log.error("Failed to upload artifact file to dataset {}", artifactId);
			if(res.statusText().isEmpty()) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
					.build();
			}
			return Response.status(res.statusCode(), res.statusText().get()).type(MediaType.APPLICATION_JSON)
				.build();
		}
	}


}
