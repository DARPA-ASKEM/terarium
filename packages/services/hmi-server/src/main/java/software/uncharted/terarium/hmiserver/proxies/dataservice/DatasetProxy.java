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
	@Path("/features")
	Response getFeatures(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createFeatures(
		JsonNode feature
	);

	@GET
	@Path("/features/{id}")
	Response getFeature(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/features/{id}")
	Response deleteFeature(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/features/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateFeature(
		@PathParam("id") String id,
		JsonNode feature
	);

	@GET
	@Path("/qualifiers")
	Response getQualifiers(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Path("/qualifiers")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createQualifiers(
		JsonNode qualifier
	);

	@GET
	@Path("/qualifiers/{id}")
	Response getQualifier(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/qualifiers/{id}")
	Response deleteQualifier(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/qualifiers/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateQualifier(
		@PathParam("id") String id,
		JsonNode qualifier
	);

	@GET
	List<Dataset> getDatasets(
		@DefaultValue("1000") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createDatasets(
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
