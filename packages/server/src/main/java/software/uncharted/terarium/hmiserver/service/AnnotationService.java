package software.uncharted.terarium.hmiserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.entities.Annotation;
import software.uncharted.terarium.hmiserver.repository.AnnotationRepository;

import java.util.List;

@Service
@Slf4j
public class AnnotationService {

	@Autowired
	private AnnotationRepository annotationRepository;

	public List<Annotation> findArtifacts(final String artifactType, final String artifactId, final int limit) {
		final Pageable pagable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestampMillis"));
		return annotationRepository.findAllByArtifactTypeAndArtifactId(artifactType, artifactId, pagable);
	}

	public Annotation findArtifact(final String artifactId) {
		return annotationRepository.findByArtifactId(artifactId);
	}

	public Annotation save(final Annotation a) {
		return annotationRepository.save(a);
	}

	public void delete(final String artifactId) {
		annotationRepository.deleteByArtifactId(artifactId);
	}

}
