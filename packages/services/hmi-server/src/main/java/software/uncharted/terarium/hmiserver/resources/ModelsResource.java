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
			// TEMP CODE:
			//  models are not returned to client yet since the client expects a different data structure
			// FIXME: return the list of models obtained through getModels and remove getMockModels
			final List<Model> models = modelService.getModels();
			System.out.println("Received " + models.size() + " from the DB!");

			final List<Model> mockModels = modelService.getMockModels();
			return Response.ok(mockModels).build();
		}

}
