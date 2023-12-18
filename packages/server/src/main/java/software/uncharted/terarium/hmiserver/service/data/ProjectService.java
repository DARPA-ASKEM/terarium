package software.uncharted.terarium.hmiserver.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.data.project.Project;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectService {

	final ProjectRepository projectRepository;

	public List<Project> getProjects(){
		return projectRepository.findAll();
	}

	public List<Project> getActiveProjects(final List<UUID> ids){
		return projectRepository.findAllByIdAndDeletedOnIsNull(ids);
	}

	public List<Project> getProjects(final List<UUID> ids){ return projectRepository.findAllById(ids);}

	public Optional<Project> getProject(final UUID id){
		return projectRepository.findById(id);
	}

	public Project save(final Project project){
		return projectRepository.save(project);
	}

	public boolean delete(final UUID id){
		final Optional<Project> project = getProject(id);
		if (project.isEmpty())
			return false;
		project.get().setDeletedOn(Timestamp.from(Instant.now()));
		return (save(project.get()) != null);
	}
}
