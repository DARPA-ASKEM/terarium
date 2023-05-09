package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.entities.Annotation;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.Instant;

@Path("/api/annotations")

@Tag(name = "User Annotation REST Endpoints")
@Slf4j
public class AnnotationResource {
	@Inject
	SecurityIdentity securityIdentity;

	@GET
	public Response getAnnotations(
		@QueryParam("artifact_type") final String artifactType,
		@QueryParam("artifact_id") final String artifactId) {

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

	@PATCH
	@Transactional
	public Response updateAnnotation(final Annotation annotation){
		String id = annotation.getId();
		String content = annotation.getContent();
		String section = annotation.getSection();
		if (id == null || content == null || section == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		Annotation entity = Annotation.findById(id);
		if (entity == null){
			throw new NotFoundException();
		}
		entity.setContent(content);
		entity.setSection(section);
		entity.setTimestampMillis(Instant.now().toEpochMilli());
		Annotation.persist(entity);
		return Response.ok().build();
	}

	@DELETE
	@Transactional
	public Response deleteAnnotations(@QueryParam("id") final String id) {
		if (id == null) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		Annotation entity = Annotation.findById(id);
		if (entity == null){
			throw new NotFoundException();
		}
		entity.delete();
		return Response.ok().build();
	}
}

