package software.uncharted.terarium.hmiserver.service.data;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {

	final ProjectRepository projectRepository;
	final UserRepository userRepository;

	public List<Project> getProjects() {
		return projectRepository.findAll();
	}

	public List<Project> getProjects(final List<UUID> ids) {
		return projectRepository.findAllById(ids);
	}

	public List<Project> getActiveProjects(final List<UUID> ids) {
		return projectRepository.findAllByIdInAndDeletedOnIsNull(ids);
	}

	public Optional<Project> getProject(final UUID id) {
		final Optional<Project> project = projectRepository.findById(id);
		if (project.isPresent()) {
			final Optional<User> user = userRepository.findById(project.get().getUserId());
			user.ifPresent(value -> project.get().setUserName(value.getName()));
		}
		return project;
	}

	public Project createProject(final Project project) {
		return projectRepository.save(project);
	}

	public Optional<Project> updateProject(final Project project) {
		if (!projectRepository.existsById(project.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectRepository.save(project));
	}

	public boolean delete(final UUID id) {
		final Optional<Project> project = getProject(id);
		if (project.isEmpty())
			return false;
		project.get().setDeletedOn(Timestamp.from(Instant.now()));
		projectRepository.save(project.get());
		return true;
	}
}
