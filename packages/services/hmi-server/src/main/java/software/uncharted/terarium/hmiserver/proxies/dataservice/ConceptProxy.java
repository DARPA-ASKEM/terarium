package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/concepts")
@Produces(MediaType.APPLICATION_JSON)
public interface ConceptProxy {
	@GET
	Response searchConcept(
		@QueryParam("term") String term,
		@QueryParam("limit") Integer limit,
		@QueryParam("offset") Integer offset
	);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	Response createConcept(
		Concept concept
	);

	@GET
	@Path("/definition/{curie}")
	Response getConceptDefinition(
		@PathParam("curie") String id
	);

	@GET
	@Path("/{id}")
	Response getConcept(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	Response deleteConcept(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	Response updateConcept(
		@PathParam("id") String id,
		Concept concept
	);
}
