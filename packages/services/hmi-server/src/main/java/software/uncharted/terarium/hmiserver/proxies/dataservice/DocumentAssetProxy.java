package software.uncharted.terarium.hmiserver.proxies.dataservice;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.DocumentAsset;

@RegisterRestClient(configKey = "data-service")
@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
public interface DocumentAssetProxy {

    @GET
    @LogRestClientTime
	List<DocumentAsset> getDocuments(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);
    
    @GET
	@Path("/{id}")
    @LogRestClientTime
	DocumentAsset getDocument(
		@PathParam("id") String id
	);
}
