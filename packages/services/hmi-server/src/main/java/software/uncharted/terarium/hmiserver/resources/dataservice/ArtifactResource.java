package software.uncharted.terarium.hmiserver.resources.dataservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ArtifactProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.resources.SnakeCaseResource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Path("/api/artifacts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Artifacts")
@Slf4j
public class ArtifactResource implements SnakeCaseResource {

	@Inject
	@RestClient
	ArtifactProxy artifactProxy;

	@RestClient
	@Inject
	JsDelivrProxy gitHubProxy;


	@GET
	@Tag(name = "Get Artifacts from TDS Proxy")
	public List<Artifact> getArtifacts(@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
															@DefaultValue("0") @QueryParam("page") Integer page) {
		return artifactProxy.getArtifacts(pageSize, page);
	}

	@PUT
	@Tag(name = "Create Artifact in TDS Proxy")
	public Artifact createArtifact(Artifact artifact) {
		return artifactProxy.createArtifact(convertObjectToSnakeCaseJsonNode(artifact));
	}

	@GET
	@Path("/{id}")
	@Tag(name = "Get specific Artifact from TDS Proxy")
	public Artifact getArtifact(@PathParam("id") String artifactId) {
		return artifactProxy.getArtifact(artifactId);
	}

	@PUT
	@Path("/{id}")
	@Tag(name = "Update Artifact in TDS Proxy")
	public Artifact updateArtifact(@PathParam("id") String artifactId, Artifact artifact) {
		return artifactProxy.updateArtifact(artifactId, artifact);
	}

	@DELETE
	@Path("/{id}")
	@Tag(name = "Delete Artifact from TDS Proxy")
	public Response deleteArtifact(@PathParam("id") String artifactId) {
		return artifactProxy.deleteArtifact(artifactId);
	}


	@GET
	@Path("/{id}/download-file-as-text")
	@Tag(name = "Download Artifact file from TDS Proxy as text")
	public Response downloadFileAsText(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final PresignedURL presignedURL = artifactProxy.getArtifactDownloadUrl(artifactId, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return Response.status(Response.Status.OK).entity(textFileAsString).type(MediaType.TEXT_PLAIN).build();

		} catch (Exception e) {
			log.error("Unable to GET file as string data", e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

	}

	@GET
	@Path("/{id}/download-file")
	@Tag(name = "Download Artifact file from TDS Proxy")
	public Response downloadFile(@PathParam("id") String artifactId, @QueryParam("filename") String filename) {

		log.debug("Downloading artifact {} from project", artifactId);


		try (CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = artifactProxy.getArtifactDownloadUrl(artifactId, filename);
			final HttpGet get = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(get);
			if(response.getStatusLine().getStatusCode() == 200 && response.getEntity() != null) {
				byte[] fileAsBytes = response.getEntity().getContent().readAllBytes();
				return Response.status(Response.Status.OK).entity(fileAsBytes).type(MediaType.APPLICATION_OCTET_STREAM).build();
			}
			return Response.status(response.getStatusLine().getStatusCode()).build();

		} catch (Exception e) {
			log.error("Unable to GET artifact data", e);
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON)
					.build();
		}

	}

	@PUT
	@Path("/{artifactId}/uploadFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Tag(name = "Upload Artifact file to TDS Proxy")
	public Response uploadFile(
		@PathParam("artifactId") final String artifactId,
		@QueryParam("filename") final String filename,
		Map<String, InputStream> input
	) throws IOException {

		log.debug("Uploading artifact {} to project", artifactId);

		byte[] fileAsBytes = input.get("file").readAllBytes();
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadArtifactHelper(artifactId, filename, fileEntity);


	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 */
	@PUT
	@Path("/{artifactId}/uploadArtifactFromGithub")
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Upload Artifact from Github to TDS Proxy")
	public Response uploadArtifactFromGithub(
		@PathParam("artifactId") final String artifactId,
		@QueryParam("path") final String path,
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("filename") final String filename
	){
		log.debug("Uploading artifact file from github to dataset {}", artifactId);

		//download file from GitHub
		String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path);
		HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadArtifactHelper(artifactId, filename, fileEntity);

	}

	/**
	 * Uploads an artifact inside the entity to TDS via a presigned URL
	 * @param artifactId The ID of the artifact to upload to
	 * @param fileName The name of the file to upload
	 * @param artifactHttpEntity The entity containing the artifact to upload
	 * @return A response containing the status of the upload
	 */
	private Response uploadArtifactHelper(String artifactId, String fileName, HttpEntity artifactHttpEntity){

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = artifactProxy.getArtifactUploadUrl(artifactId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(artifactHttpEntity);
			final HttpResponse response = httpclient.execute(put);
;

			return Response
				.status(response.getStatusLine().getStatusCode())
				.type(MediaType.APPLICATION_JSON)
				.build();


		} catch (Exception e) {
			log.error("Unable to PUT artifact data", e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}
	}






}
