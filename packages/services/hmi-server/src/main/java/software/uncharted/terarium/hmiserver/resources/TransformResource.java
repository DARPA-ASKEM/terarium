package software.uncharted.terarium.hmiserver.resources;

import software.uncharted.terarium.hmiserver.proxies.skema.SkemaRustProxy;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/api/transforms")
@Authenticated
@Slf4j
public class TransformResource {
	@Inject
	@RestClient
	SkemaRustProxy skemaProxy;

	@POST
	@Path("mathml-2-acset")
	public Response mathML2ACSet(List<String> list) {
		return skemaProxy.convertMathML2ACSet(list);
	}
}
