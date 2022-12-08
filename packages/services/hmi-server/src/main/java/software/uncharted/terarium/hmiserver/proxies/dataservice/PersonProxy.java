package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Association;
import software.uncharted.terarium.hmiserver.models.dataservice.Person;

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
		@PathParam("id") String id,
		Association association
	);

	@POST
	@Path("/associations")
	@Consumes(MediaType.APPLICATION_JSON)
	Response createAssociation(
		Association association
	);

	@GET
	Response getPersons(
		@DefaultValue("100") @QueryParam("page_size") Integer pageSize,
		@DefaultValue("0") @QueryParam("page") Integer page
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createPerson(
		Person person
	);

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
		@PathParam("id") String id,
		Person person
	);
}
