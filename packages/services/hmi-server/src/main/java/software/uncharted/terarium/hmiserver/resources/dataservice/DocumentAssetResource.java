package software.uncharted.terarium.hmiserver.resources.dataservice;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import software.uncharted.terarium.hmiserver.models.dataservice.DocumentAsset;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DocumentAssetProxy;

@Path("/api/document-asset")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Documents2 REST Endpoints")
public class DocumentAssetResource {
    @Inject
	@RestClient
	DocumentAssetProxy proxy;

    @GET
	@Tag(name = "Get all documents via TDS proxy")
	public List<DocumentAsset> getDocuments(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getDocuments(pageSize, page);
	}

    @GET
	@Path("/{id}")
	@Tag(name = "Get a specific document via TDS proxy")
	public DocumentAsset getDocument(
		@PathParam("id") final String id
	) {
		return proxy.getDocument(id);
	}
    
}
