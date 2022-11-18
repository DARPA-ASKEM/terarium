package software.uncharted.terarium.hmiserver.proxies;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
public interface PersonProxy {
	@GET
	@Path("/associations/{id}")
	Response getAssociation(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/associations/{id}")
	Response deleteAssociation(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/associations/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateAssociation(
		@PathParam("id") String id
	);

	@POST
	@Path("/associations")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createAssociation();

	@GET
	Response getPersons(
		@QueryParam("page_size") Integer pageSize,
		@QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createPerson();

	@GET
	@Path("/{id}")
	Response getPerson(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	Response deletePerson(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updatePerson(
		@PathParam("id") String id
	);
}
