package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RegisterRestClient(configKey = "data-service")
@Path("/datasets")
@Produces(MediaType.APPLICATION_JSON)
public interface DatasetProxy {

	@LogRestClientTime
	@GET
	List<Dataset> getDatasets(
		@DefaultValue("500") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createDataset(
		JsonNode dataset
	);

	@GET
	@Path("/{id}")
	Dataset getDataset(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	Response deleteDataset(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateDataset(
		@PathParam("id") String id,
		JsonNode dataset
	);

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deprecateDataset(
		@PathParam("id") String id
	);


	@GET
	@Path("/{id}/upload-url")
	PresignedURL getUploadUrl(
		@PathParam("id") String id,
		@QueryParam("filename") String filename
	);

	@GET
	@Path("/{id}/download-url")
	PresignedURL getDownloadUrl(
		@PathParam("id") String id, @QueryParam("filename") String filename
	);
}
