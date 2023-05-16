package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.Feature;
import software.uncharted.terarium.hmiserver.models.dataservice.Qualifier;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
	List<Dataset> getDatasets(
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
		Dataset dataset
	);

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response deprecateDataset(
		@PathParam("id") String id
	);

	@GET
	@Path("/{id}/file")
	Response getCsv(
		@PathParam("id") String id,
		@DefaultValue("true") @QueryParam("wide_format") final Boolean wideFormat,
		@DefaultValue("50") @QueryParam("row_limit") final Integer rowLimit
	);

	@POST
	@Path("/{id}/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	Response uploadFile(
		@PathParam("id") String id,
		@QueryParam("filename") String filename,
		@FormDataParam("file") MultipartFormDataOutput file
	);
}
