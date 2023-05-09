package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Intermediate;
import software.uncharted.terarium.hmiserver.models.dataservice.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelStub;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelFramework;
import software.uncharted.terarium.hmiserver.models.dataservice.ModelOperationCopy;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.models.petrinet.Ontology;
import software.uncharted.terarium.hmiserver.models.petrinet.Species;
import software.uncharted.terarium.hmiserver.proxies.dataservice.ModelProxy;
import software.uncharted.terarium.hmiserver.proxies.mira.DKGProxy;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.smallrye.jwt.config.ConfigLogging.log;

@Path("/api/models")

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoints")
public class ModelResource {

	@ConfigProperty(name = "mira-metaregistry/mp-rest/url")
	String metaRegistryURL;

	@Inject
	@RestClient
	ModelProxy proxy;

	@Inject
	@RestClient
	DKGProxy dkgProxy;

	@POST
	@Path("/frameworks")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createFramework(
		final ModelFramework framework
	) {
		return proxy.createFramework(framework);
	}

	@GET
	@Path("/frameworks/{name}")
	public Response getFramework(
		@PathParam("name") final String name
	) {
		return proxy.getFramework(name);
	}

	@DELETE
	@Path("/frameworks/{name}")
	public Response deleteFramework(
		@PathParam("name") final String name
	) {
		return proxy.deleteFramework(name);
	}

	@GET
	@Path("/intermediates/{id}")
	public Response getIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.getIntermediate(id);
	}

	@DELETE
	@Path("/intermediates/{id}")
	public Response deleteIntermediate(
		@PathParam("id") final String id
	) {
		return proxy.deleteIntermediate(id);
	}

	@POST
	@Path("/intermediates")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createIntermediate(
		final Intermediate intermediate
	) {
		return proxy.createIntermediate(intermediate);
	}

	@GET
	@Path("/descriptions")
	public Response getDescriptions(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getDescriptions(pageSize, page);
	}

	@GET
	@Path("/{id}/descriptions")
	public Response getDescription(
		@PathParam("id") final String id
	) {
		return proxy.getDescription(id);
	}

	@GET
	@Path("/{id}/parameters")
	public Response getParameters(
		@PathParam("id") final String id
	) {
		return proxy.getParameters(id);
	}

	@PUT
	@Path("/parameters/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateParameters(
		@PathParam("id") final String id,
		final Map<String, Object> parameters
	) {
		return proxy.updateParameters(id, parameters);
	}

	/**
	 * Get a Model from the data-service
	 * Return Model
	 */
	@GET
	@Path("/{id}")
	@APIResponses({
		@APIResponse(responseCode = "500", description = "An error occurred retrieving the model"),
		@APIResponse(responseCode = "204", description = "Request received successfully, but there are no model with this id")
	})
	public Response getModel(
		@PathParam("id") final String id
	) {
		Model model;

		// Fetch the model from the data-service
		try {
			model = proxy.getModel(id);
		} catch (RuntimeException e) {
			log.error("Unable to get the model" + id, e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.build();
		}

		if (model == null) {
			return Response.noContent().build();
		}

		// Resolve the ontology curies
		final List<Species> species = model.getContent().getS();

		if (species != null && !species.isEmpty()) {
			// Get the curies from all species, as one string, comma separated without duplicate
			final String curies = species.stream()
				.flatMap(s -> Stream.concat(
					s.getMiraIds().stream().map(Ontology::getCurie),
					s.getMiraContext().stream().map(Ontology::getCurie)
				))
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.joining(","));

			// Fetch the ontology information from the DKG
			List<DKG> entities = new ArrayList<>();
			try {
				entities = dkgProxy.getEntities(curies);
			} catch (RuntimeException e) {
				log.error("Unable to get the ontology entity for curies: " + curies, e);
			}

			if (!entities.isEmpty()) {
				entities.forEach(entity -> entity.setLink(metaRegistryURL + "/" + entity.getCurie()));

				// Transform the entities to a Map
				Map<String, DKG> ontologies =
					entities.stream().collect(Collectors.toMap(DKG::getCurie, entity -> entity));

				// Now add the ontologies to each species mira_ids and mira_context
				species.forEach(s -> {
					s.getMiraIds().forEach(miraId -> {
						if (ontologies.containsKey(miraId.getCurie())) {
							final DKG ontology = ontologies.get(miraId.getCurie());
							miraId
								.setTitle(ontology.getName())
								.setDescription(ontology.getDescription())
								.setLink(ontology.getLink());
						}
					});

					s.getMiraContext().forEach(context -> {
						if (ontologies.containsKey(context.getCurie())) {
							final DKG ontology = ontologies.get(context.getCurie());
							context
								.setTitle(ontology.getName())
								.setDescription(ontology.getDescription())
								.setLink(ontology.getLink());
						}
					});
				});
			}
		}

		// Return the model
		return Response
			.status(Response.Status.OK)
			.entity(model)
			.build();
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateModel(
		@PathParam("id") final String id,
		final ModelStub model
	) {
		return proxy.updateModel(id, model);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createModel(
		final ModelStub model
	) {
		return proxy.createModel(model);
	}

	@POST
	@Path("/{id}/operation-copy")
	public Response saveAsNewModel(
		@PathParam("id") final String id,
		final ModelOperationCopy modelOperationCopy) {
		return proxy.copyModel(modelOperationCopy);
	}
}
