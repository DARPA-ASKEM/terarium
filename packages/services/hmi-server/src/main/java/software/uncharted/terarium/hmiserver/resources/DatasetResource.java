package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.proxies.DatasetProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/datasets")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Project REST Endpoints")
public class DatasetResource {

	@Inject
	@RestClient
	DatasetProxy proxy;

	@GET
	@Path("/features")
	public Response getFeatures() {
		return proxy.getFeatures();
	}

	@POST
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createFeatures() {
		return proxy.createFeatures();
	}

	@GET
	@Path("/features/{id}")
	public Response getFeature(
		@PathParam("id") final String id
	) {
		return proxy.getFeature(id);
	}

	@DELETE
	@Path("/features")
	public Response deleteFeature() {
		return proxy.deleteFeature();
	}

	@PATCH
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateFeature() {
		return proxy.updateFeature();
	}

	@GET
	@Path("/qualifiers")
	public Response getQualifiers() {
		return proxy.getQualifiers();
	}

	@POST
	@Path("/qualifiers")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createQualifiers() {
		return proxy.createQualifiers();
	}

	@GET
	@Path("/qualifiers/{id}")
	public Response getQualifier(
		@PathParam("id") final String id
	) {
		return proxy.getQualifier(id);
	}

	@DELETE
	@Path("/qualifiers/{id}")
	public Response deleteQualifier(
		@PathParam("id") final String id
	) {
		return proxy.deleteQualifier(id);
	}

	@PATCH
	@Path("/qualifiers/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQualifier(
		@PathParam("id") final String id
	) {
		return proxy.updateQualifier(id);
	}

	@GET
	public Response getDatasets() {
		return proxy.getDatasets();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDatasets() {
		return proxy.createDatasets();
	}

	@GET
	@Path("/{id}")
	public Response getDataset(
		@PathParam("id") final String id
	) {
		return proxy.getDataset(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataset(
		@PathParam("id") final String id
	) {
		return proxy.deleteDataset(id);
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataset(
		@PathParam("id") final String id
	) {
		return proxy.updateDataset(id);
	}

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deprecateDataset(
		@PathParam("id") final String id
	) {
		return proxy.deprecateDataset(id);
	}

	@GET
	@Path("/{id}/download/csv")
	public Response getCsv(
		@PathParam("id") final String id
	) {
		return proxy.getCsv(id);
	}
}
