package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ConceptProxy;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/concepts")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Concept REST Endpoints")
public class ConceptResource {

	@Inject
	@RestClient
	ConceptProxy proxy;

	@GET
	public Response searchConcept(
		@QueryParam("term") final String term,
		@QueryParam("limit") final Integer limit,
		@QueryParam("offset") final Integer offset
	) {
		return proxy.searchConcept(term, limit, offset);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createConcept(
		final Concept concept
	) {
		return proxy.createConcept(concept);
	}

	@GET
	@Path("/definition/{curie}")
	public Response getConceptDefinition(
		@PathParam("curie") final String id
	) {
		return proxy.getConceptDefinition(id);
	}

	@GET
	@Path("/{id}")
	public Response getConcept(
		@PathParam("id") final String id
	) {
		return proxy.getConcept(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteConcept(
		@PathParam("id") final String id
	) {
		return proxy.deleteConcept(id);
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateConcept(
		@PathParam("id") final String id,
		final Concept concept
	) {
		return proxy.updateConcept(id, concept);
	}

	@GET
	@Path("/facets")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchConceptsUsingFacets(
		@QueryParam("types") final List<String> types,
		@QueryParam("curies") final List<String> curies
	) {
		return proxy.searchConceptsUsingFacets(types, curies);
	}
}
