package software.uncharted.terarium.hmiserver.specification;

import org.springframework.data.jpa.domain.Specification;
import software.uncharted.terarium.hmiserver.models.dataservice.Project;

import java.util.UUID;

public class ProjectSpecification {

	public static Specification<Project> hasNameLike(String name) {
		return (root, query, criteriaBuilder) ->
			criteriaBuilder.like(root.get("name"), "%" + name + "%");
	}

	public static Specification<Project> hasName(String name) {
		return (root, query, cb) ->
			cb.equal(root.<String>get("name"), name);
	}

	public static Specification<Project> hasId(UUID id) {
		return (root, query, cb) ->
			cb.equal(root.<UUID>get("id"), id);
	}
}
