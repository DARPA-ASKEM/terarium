package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.user.Annotation;

@Repository
public interface AnnotationRepository extends PSCrudRepository<Annotation, String> {
	List<Annotation> findAllByArtifactTypeAndArtifactId(
		final String artifactType,
		final String artifactId,
		Pageable pagable
	);

	Annotation findByArtifactId(final String artifactId);

	void deleteByArtifactId(final String artifactId);
}
