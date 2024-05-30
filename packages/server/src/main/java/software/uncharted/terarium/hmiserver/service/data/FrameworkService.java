package software.uncharted.terarium.hmiserver.service.data;

import io.micrometer.observation.annotation.Observed;
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

	@Observed(name = "function_profile")
	public List<ModelFramework> getFrameworks() {
		return frameworkRepository.findAllByDeletedOnIsNull();
	}

	@Observed(name = "function_profile")
	public List<ModelFramework> getFrameworks(final List<UUID> ids) {
		return frameworkRepository.findAllByIdInAndDeletedOnIsNull(ids);
	}

	@Observed(name = "function_profile")
	public Optional<ModelFramework> getFramework(final UUID id) {
		return frameworkRepository.getByIdAndDeletedOnIsNull(id);
	}

	@Observed(name = "function_profile")
	public ModelFramework createFramework(final ModelFramework framework) {
		return frameworkRepository.save(framework);
	}

	@Observed(name = "function_profile")
	public Optional<ModelFramework> updateFramework(final ModelFramework framework) {
		if (!frameworkRepository.existsById(framework.getId())) {
			return Optional.empty();
		}
		return Optional.of(frameworkRepository.save(framework));
	}

	@Observed(name = "function_profile")
	public void deleteFramework(final UUID id) {
		final Optional<ModelFramework> framework = frameworkRepository.findById(id);
		if (framework.isEmpty()) {
			return;
		}
		framework.get().setDeletedOn(Timestamp.from(Instant.now()));
		frameworkRepository.save(framework.get());
	}
}
