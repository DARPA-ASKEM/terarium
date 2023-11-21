package software.uncharted.terarium.hmiserver.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;

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

	public Optional<Project> getProject(final UUID id){
		return projectRepository.findById(id);
	}

	public Project save(final Project project){
		return projectRepository.save(project);
	}

	public void delete(final UUID id){
		projectRepository.deleteById(id);
	}
}
