package software.uncharted.terarium.hmiserver.service.data;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;
import software.uncharted.terarium.hmiserver.repository.data.FrameworkRepository;

@Service
@RequiredArgsConstructor
public class FrameworkService {

	final FrameworkRepository frameworkRepository;

	public List<ModelFramework> getFrameworks() {
		return frameworkRepository.findAll();
	}

	public List<ModelFramework> getFrameworks(final List<String> names) {
		return frameworkRepository.findAllById(names);
	}

	public Optional<ModelFramework> getFramework(final String name) {
		return frameworkRepository.findById(name);
	}

	public ModelFramework createFramework(final ModelFramework framework) {
		return frameworkRepository.save(framework);
	}

	public ModelFramework updateFramework(final ModelFramework framework) {
		return frameworkRepository.save(framework);
	}

	public void deleteFramework(final String name) {
		frameworkRepository.deleteById(name);
	}

}
