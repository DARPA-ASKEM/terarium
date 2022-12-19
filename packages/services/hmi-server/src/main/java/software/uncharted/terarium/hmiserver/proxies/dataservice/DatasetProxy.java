package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.Feature;
import software.uncharted.terarium.hmiserver.models.dataservice.Qualifier;

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
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createFeatures(
		Feature feature
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
		Feature feature
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
		Qualifier qualifier
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
		Qualifier qualifier
	);

	@GET
	Response getDatasets(
		@DefaultValue("500") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createDatasets(
		Dataset dataset
	);

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
		@PathParam("id") String id,
		Dataset dataset
	);

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deprecateDataset(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}/download/rawfile")
	Response getCsv(
		@PathParam("id") String id,
		@DefaultValue("true") @QueryParam("wide_format") final Boolean wideFormat,
		@DefaultValue("false") @QueryParam("data_annotation_flag") Boolean dataAnnotationFlag
	);

	@POST
	@Path("/{id}/upload/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	Response uploadFile(
		@PathParam("id") String id,
		@QueryParam("filename") String filename,
		Byte[] file
	);
}
