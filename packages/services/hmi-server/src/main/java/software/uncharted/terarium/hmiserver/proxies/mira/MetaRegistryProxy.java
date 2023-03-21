package software.uncharted.terarium.hmiserver.proxies.mira;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "mira-metaregistry")
@Produces(MediaType.APPLICATION_JSON)
public interface MetaRegistryProxy {

}
