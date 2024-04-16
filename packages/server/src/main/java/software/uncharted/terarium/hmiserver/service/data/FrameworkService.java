package software.uncharted.terarium.hmiserver.service.data;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.repository.data.FrameworkRepository;

@Service
@RequiredArgsConstructor
public class FrameworkService {

	final FrameworkRepository frameworkRepository;

	public List<ModelFramework> getFrameworks() {
		return frameworkRepository.findAllByDeletedOnIsNull();
	}

	public List<ModelFramework> getFrameworks(final List<UUID> ids) {
		return frameworkRepository.findAllByIdInAndDeletedOnIsNull(ids);
	}

	public Optional<ModelFramework> getFramework(final UUID id) {
		return frameworkRepository.getByIdAndDeletedOnIsNull(id);
	}

	public ModelFramework createFramework(final ModelFramework framework) {
		return frameworkRepository.save(framework);
	}

	public Optional<ModelFramework> updateFramework(final ModelFramework framework) {
		if (!frameworkRepository.existsById(framework.getId())) {
			return Optional.empty();
		}
		return Optional.of(frameworkRepository.save(framework));
	}

	public void deleteFramework(final UUID id) {
		Optional<ModelFramework> framework = frameworkRepository.findById(id);
		if (framework.isEmpty()) {
			return;
		}
		framework.get().setDeletedOn(Timestamp.from(Instant.now()));
		frameworkRepository.save(framework.get());
	}
}
