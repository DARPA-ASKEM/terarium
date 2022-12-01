package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Association;
import software.uncharted.terarium.hmiserver.models.dataservice.Person;
import software.uncharted.terarium.hmiserver.proxies.dataservice.PersonProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/persons")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Person REST Endpoints")
public class PersonResource {

	@Inject
	@RestClient
	PersonProxy proxy;

	@GET
	@Path("/associations/{id}")
	public Response getAssociation(
		@PathParam("id") final String id
	) {
		return proxy.getAssociation(id);
	}

	@DELETE
	@Path("/associations/{id}")
	public Response deleteAssociation(
		@PathParam("id") final String id
	) {
		return proxy.deleteAssociation(id);
	}

	@PATCH
	@Path("/associations/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAssociation(
		@PathParam("id") final String id,
		final Association association
	) {
		return proxy.updateAssociation(id, association);
	}

	@POST
	@Path("/associations")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createAssociation(
		final Association association
	) {
		return proxy.createAssociation(association);
	}

	@GET
	public Response getPersons(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getPersons(pageSize, page);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPerson(
		final Person person
	) {
		return proxy.createPerson(person);
	}

	@GET
	@Path("/{id}")
	public Response getPerson(
		@PathParam("id") final String id
	) {
		return proxy.getPerson(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deletePerson(
		@PathParam("id") final String id
	) {
		return proxy.deletePerson(id);
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePerson(
		@PathParam("id") final String id,
		final Person person
	) {
		return proxy.updatePerson(id, person);
	}
}
