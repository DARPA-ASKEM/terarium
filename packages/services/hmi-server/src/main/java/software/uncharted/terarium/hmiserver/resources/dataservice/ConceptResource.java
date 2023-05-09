package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import software.uncharted.terarium.hmiserver.models.dataservice.Concept;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ConceptProxy;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/concepts")

@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Concept REST Endpoints")
@RegisterProvider(HmiResponseExceptionMapper.class)
@Slf4j
public class ConceptResource {

	@Inject
	@RestClient
	ConceptProxy proxy;

	@GET
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred fetching search concept")})
	public Response searchConcept(
		@QueryParam("curie") final String curie
	) {
		try {
			return proxy.searchConcept(curie);
		} catch (RuntimeException e) {
			log.error("Unable to get search concept", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(responseCode = "500", description = "Unable to create a concept")})
	public Response createConcept(
		final Concept concept
	) {
		try {
			return proxy.createConcept(concept);
		} catch (RuntimeException e) {
			log.error("Unable to create a concept", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/definitions")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error searching concept definitions")})
	public Response searchConceptDefinitions(
		@QueryParam("term") final String term,
		@DefaultValue("100") @QueryParam("limit") final Integer limit,
		@DefaultValue("0") @QueryParam("offset") final Integer offset
	) {
		try {
			return proxy.searchConceptDefinitions(term, limit, offset);
		} catch (RuntimeException e) {
			log.error("An error searching concept definitions", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/definitions/{curie}")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error getting concept definitions")})
	public Response getConceptDefinitions(
		@PathParam("curie") final String curie
	) {
		try {
			return proxy.getConceptDefinitions(curie);
		} catch (RuntimeException e) {
			log.error("An error getting concept definitions", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
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
	@APIResponses({
		@APIResponse(responseCode = "500", description = "Unable to delete concept")})
	public Response deleteConcept(
		@PathParam("id") final String id
	) {
		try {
			return proxy.deleteConcept(id);
		} catch (RuntimeException e) {
			log.error("Unable to delete concept", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(responseCode = "500", description = "Unable to update concept")})
	public Response updateConcept(
		@PathParam("id") final String id,
		final Concept concept
	) {
		try {
			return proxy.updateConcept(id, concept);
		} catch (RuntimeException e) {
			log.error("Unable to update concept", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/facets")
	@Consumes(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(responseCode = "500", description = "Unable to search concept using facets")})
	public Response searchConceptsUsingFacets(
		@QueryParam("types") final List<String> types,
		@QueryParam("curies") final List<String> curies
	) {
		try {
			return proxy.searchConceptsUsingFacets(types, curies);
		} catch (RuntimeException e) {
			log.error("Unable to search concept using facets", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
