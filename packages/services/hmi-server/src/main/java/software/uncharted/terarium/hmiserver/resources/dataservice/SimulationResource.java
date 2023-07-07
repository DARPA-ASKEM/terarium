package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

import software.uncharted.terarium.hmiserver.proxies.dataservice.SimulationProxy;
import software.uncharted.terarium.hmiserver.models.dataservice.Simulation;
import software.uncharted.terarium.hmiserver.utils.Converter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api/simulations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Simulation REST Endpoints")
public class SimulationResource {

	@Inject
	@RestClient
	SimulationProxy proxy;

	@POST
	public Simulation createSimulation(final Simulation simulation){
		return proxy.createSimulation(Converter.convertObjectToSnakeCaseJsonNode(simulation));
	}

	@GET
	@Path("/{id}")
	public Response getSimulation(
		@PathParam("id") final String id
	) {
		return Response
			.ok(Response.Status.OK)
			.entity(proxy.getSimulation(id))
			.build();
	}

	@PUT
	@Path("/{id}")
	public Simulation updateSimulation(@PathParam("id") final String id, final Simulation simulation){
		return proxy.updateSimulation(id, simulation);
	}

	@DELETE
	@Path("/{id}")
	public String deleteSimulation(@PathParam("id") final String id){
		return proxy.deleteSimulation(id);
	}


	@GET
	@Path("/{id}/result")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSimulation(
		@PathParam("id") final String id,
		@QueryParam("filename") final String filename
	) throws Exception {
		CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build();

		final PresignedURL presignedURL = proxy.getDownloadURL(id, filename);
		final HttpGet get = new HttpGet(presignedURL.getUrl());
		final HttpResponse response = httpclient.execute(get);
		final String data = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

		// return response;
		return Response
			.ok(Response.Status.OK)
			.entity(data)
			.build();
	}



}
