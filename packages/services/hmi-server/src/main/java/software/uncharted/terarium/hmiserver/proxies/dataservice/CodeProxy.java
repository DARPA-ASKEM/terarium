package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Proxies the TDS code endpoints
 */
@RegisterRestClient(configKey = "data-service")
@Path("/code")
@Produces("application/json")
@Consumes("application/json")
public interface CodeProxy {

    	@GET
		@LogRestClientTime
    	List<Code> getCodes( @DefaultValue("100") @QueryParam("page_size") Integer pageSize, @DefaultValue("0") @QueryParam("page") Integer page);

		@POST
		@LogRestClientTime
		Code createCode(JsonNode code);

		@GET
		@Path("/{id}")
		@LogRestClientTime
		Code getCode(@PathParam("id") String codeId);

		@PUT
		@Path("/{id}")
		@LogRestClientTime
		Code updateCode(@PathParam("id") String codeId, Code code);

		@DELETE
		@Path("/{id}")
		@LogRestClientTime
		Response deleteCode(@PathParam("id") String codeId);

		@GET
		@Path("/{id}/download-url")
		@LogRestClientTime
		PresignedURL getCodeDownloadUrl(@PathParam("id") String codeId, @QueryParam("filename") String filename);

		@GET
		@Path("/{id}/upload-url")
		@LogRestClientTime
		PresignedURL getCodeUploadUrl(@PathParam("id") String codeId, @QueryParam("filename") String filename);


}
