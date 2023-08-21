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
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.CodeProxy;
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

@Path("/api/code-asset")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Code")
@Slf4j
public class CodeResource implements SnakeCaseResource {

	@Inject
	@RestClient
	CodeProxy codeProxy;

	@RestClient
	@Inject
	JsDelivrProxy gitHubProxy;


	@GET
	@Tag(name = "Get Codes from TDS Proxy")
	public List<Code> getCodes(@DefaultValue("100") @QueryParam("page_size") Integer pageSize, @DefaultValue("0") @QueryParam("page") Integer page) {
		return codeProxy.getCodes(pageSize, page);
	}

	@POST
	@Tag(name = "Create Code in TDS Proxy")
	public Code createCode(Code code) {

		return codeProxy.createCode(convertObjectToSnakeCaseJsonNode(code));
	}

	@GET
	@Path("/{id}")
	@Tag(name = "Get specific Code from TDS Proxy")
	public Code getCode(@PathParam("id") String codeId) {
		return codeProxy.getCode(codeId);
	}

	@PUT
	@Path("/{id}")
	@Tag(name = "Update Code in TDS Proxy")
	public Code updateCode(@PathParam("id") String codeId, Code code) {
		return codeProxy.updateCode(codeId, code);
	}

	@DELETE
	@Path("/{id}")
	@Tag(name = "Delete Code in TDS Proxy")
	public Response deleteCode(@PathParam("id") String codeId) {
		return codeProxy.deleteCode(codeId);
	}

	@GET
	@Path("/{id}/download-code-as-text")
	@Tag(name = "Get Code File from S3")
	public Response getCodeFileAsText(@PathParam("id") String codeId, @QueryParam("filename") String filename) {

		log.debug("Downloading code file {} for code {}", filename, codeId);

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			PresignedURL presignedURL = codeProxy.getCodeDownloadUrl(codeId, filename);
			final HttpGet httpGet = new HttpGet(presignedURL.getUrl());
			final HttpResponse response = httpclient.execute(httpGet);
			final String textFileAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

			return Response.status(Response.Status.OK).entity(textFileAsString).type(MediaType.TEXT_PLAIN).build();


		} catch (Exception e) {
			log.error("Unable to GET code data", e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

	}

	@PUT
	@Path("/{codeId}/uploadFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Tag(name = "Upload Code file to TDS Proxy")
	public Response uploadFile(
		@PathParam("codeId") final String codeId,
		@QueryParam("filename") final String filename,
		Map<String, InputStream> input
	) throws IOException {

		log.debug("Uploading code {} to project", codeId);

		byte[] fileAsBytes = input.get("file").readAllBytes();
		HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
		return uploadCodeHelper(codeId, filename, fileEntity);


	}

	/**
	 * Downloads a file from GitHub given the path and owner name, then uploads it to the project.
	 */
	@PUT
	@Path("/{codeId}/uploadCodeFromGithub")
	@Consumes(MediaType.APPLICATION_JSON)
	@Tag(name = "Upload Code from Github to TDS Proxy")
	public Response uploadCodeFromGithub(
		@PathParam("codeId") final String codeId,
		@QueryParam("path") final String path,
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("filename") final String filename
	){
		log.debug("Uploading code file from github to dataset {}", codeId);

		//download file from GitHub
		String fileString = gitHubProxy.getGithubCode(repoOwnerAndName, path);
		HttpEntity fileEntity = new StringEntity(fileString, ContentType.TEXT_PLAIN);
		return uploadCodeHelper(codeId, filename, fileEntity);

	}

	/**
	 * Uploads an code inside the entity to TDS via a presigned URL
	 * @param codeId The ID of the code to upload to
	 * @param fileName The name of the file to upload
	 * @param codeHttpEntity The entity containing the code to upload
	 * @return A response containing the status of the upload
	 */
	private Response uploadCodeHelper(String codeId, String fileName, HttpEntity codeHttpEntity){

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = codeProxy.getCodeUploadUrl(codeId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(codeHttpEntity);
			final HttpResponse response = httpclient.execute(put);


			return Response
				.status(response.getStatusLine().getStatusCode())
				.type(MediaType.APPLICATION_JSON)
				.build();


		} catch (Exception e) {
			log.error("Unable to PUT code data", e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}
	}

}
