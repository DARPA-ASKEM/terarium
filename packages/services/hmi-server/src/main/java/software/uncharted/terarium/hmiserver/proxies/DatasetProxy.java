package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/datasets")
@Produces(MediaType.APPLICATION_JSON)
public interface DatasetProxy {
	@GET
	@Path("/features")
	Response getFeatures(
		@QueryParam("page_size") Integer pageSize,
		@QueryParam("page") Integer page
	);

	@POST
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createFeatures();

	@GET
	@Path("/features/{id}")
	Response getFeature(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/features")
	Response deleteFeature();

	@PATCH
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateFeature();

	@GET
	@Path("/qualifiers")
	Response getQualifiers(
		@QueryParam("page_size") Integer pageSize,
		@QueryParam("page") Integer page
	);

	@POST
	@Path("/qualifiers")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createQualifiers();

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
		@PathParam("id") String id
	);

	@GET
	Response getDatasets(
		@QueryParam("page_size") Integer pageSize,
		@QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createDatasets();

	@GET
	@Path("/{id}")
	Response getDataset(
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
		@PathParam("id") String id
	);

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deprecateDataset(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}/download/csv")
	Response getCsv(
		@PathParam("id") String id
	);
}
