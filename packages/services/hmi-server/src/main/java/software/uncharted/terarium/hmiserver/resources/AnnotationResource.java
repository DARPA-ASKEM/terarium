package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.entities.Annotation;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/api/annotations")
@Authenticated
@Tag(name = "User Annotation REST Endpoints")
@Slf4j
public class AnnotationResource {
	@Inject
	SecurityIdentity securityIdentity;

	@GET
	public Response getAnnotations(
			@QueryParam("artifact_type") final String artifactType,
			@QueryParam("artifact_id") final String artifactId) {

			System.out.println("");
			System.out.println("");
			System.out.println("In getAnnotations");
			System.out.println("");
			System.out.println("");

			if (artifactType == null || artifactId == null) {
				return Response
					.status(Response.Status.BAD_REQUEST)
					.build();
			}
			return Response
				.ok(Annotation.findByArtifact(artifactType, artifactId))
				.build();
			}

	@POST
	@Transactional
	public Response postEvent(final Annotation annotation) {
		annotation.setUsername(securityIdentity.getPrincipal().getName());

		Annotation.persist(annotation);
		return Response
			.ok(annotation)
			.build();
	}
}

