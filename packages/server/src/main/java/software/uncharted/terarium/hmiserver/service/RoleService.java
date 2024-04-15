package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.*;
import software.uncharted.terarium.hmiserver.repository.AuthorityRepository;
import software.uncharted.terarium.hmiserver.repository.RoleRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final RoleRepository roleRepository;

	private final AuthorityRepository authorityRepository;

	public Role createRole(final RoleType type, Map<AuthorityType, List<AuthorityLevel>> authorities) {
		final Role role = new Role().setName(type.name());
		Set<AuthorityInstance> authorityInstances = role.getAuthorities();
		authorities.forEach((authorityType, authorityLevels) -> {
			authorityRepository.findFirstByName(authorityType.toString())
				.ifPresent(authority -> authorityInstances.add(new AuthorityInstance(authority, authorityLevels)));
		});
		return roleRepository.save(role);
	}

	public long count() {
		return roleRepository.count();
	}

	@Cacheable(value="roles", key="#roleTypes.toString()", unless="#result == null")
	public List<Role> getAllByTypes(final Set<String> roleTypes) {
		return roleRepository.findAllByNameIn(roleTypes);
	}

	@Cacheable(value="authorities", key="#roles.hashCode()", unless="#result == null")
	public List<String> getAuthorities(final Collection<Role> roles) {
		final List<String> authorities = new ArrayList<>();
		authorities.addAll(roles.stream().map(r -> "ROLE_" + r.getName()).toList());
		authorities.addAll(roles.stream()
			.map(Role::getAuthorities)
			.flatMap(Collection::stream)
			.map(AuthorityInstance::getAuthoritiesAsStrings)
			.flatMap(Collection::stream)
			.toList());
		return authorities;
	}
}
