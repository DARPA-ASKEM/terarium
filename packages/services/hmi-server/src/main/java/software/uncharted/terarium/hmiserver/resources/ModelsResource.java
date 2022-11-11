package software.uncharted.terarium.hmiserver.resources;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import software.uncharted.terarium.hmiserver.models.Model;
import software.uncharted.terarium.hmiserver.services.ModelService;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/api/model")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Model REST Endpoint")
public class ModelsResource {

    @Inject
    SecurityIdentity identity;

		@Inject
		ModelService modelService;

    @GET
		@Produces(MediaType.APPLICATION_JSON)
		@Tag(name = "Get all models") // TODO: for a given user or for a given project
		public Response getModels() {
			final List<Model> models = modelService.getModels();
			return Response.ok(models).build();
		}

}
