package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.annotations.LogRestClientTime;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "data-service")
@Path("/concepts")
@Produces(MediaType.APPLICATION_JSON)
public interface ConceptProxy {
	@GET
	@LogRestClientTime
	Response searchConcept(
		@QueryParam("curie") String curie
	);

	@POST
	@LogRestClientTime
	@Consumes(MediaType.APPLICATION_JSON)
	Response createConcept(
		Concept concept
	);

	@GET
	@Path("/definitions")
	@LogRestClientTime
	Response searchConceptDefinitions(
		@QueryParam("term") String term,
		@DefaultValue("100") @QueryParam("limit") Integer limit,
		@DefaultValue("0") @QueryParam("offset") Integer offset
	);

	@GET
	@Path("/definitions/{curie}")
	@LogRestClientTime
	Response getConceptDefinitions(
		@PathParam("curie") String curie
	);

	@GET
	@Path("/{id}")
	@LogRestClientTime
	Response getConcept(
		@PathParam("id") String id
	);

	@DELETE
	@Path("/{id}")
	@LogRestClientTime
	Response deleteConcept(
		@PathParam("id") String id
	);

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@LogRestClientTime
	Response updateConcept(
		@PathParam("id") String id,
		Concept concept
	);

	@GET
	@Path("/facets")
	@LogRestClientTime
	Response searchConceptsUsingFacets(
		@QueryParam("types") List<String> types,
		@QueryParam("curies") List<String> curies
	);
}
