package software.uncharted.terarium.hmiserver.controller.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.repository.RoleRepository;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.AdminClientService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final AdminClientService adminClientService;

	/** Get a list of all users */
	@GetMapping("/users")
	@Secured(Roles.ADMIN)
	public ResponseEntity<List<User>> getUsers(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder
	) {
		Sort sort = Sort.unsorted();
		if (sortField != null && !sortField.isEmpty()) {
			Sort.Direction direction = sortOrder < 0 ? Sort.Direction.DESC : Sort.Direction.ASC;
			sort = Sort.by(direction, sortField);
		}

		Pageable pageable = PageRequest.of(page, rows, sort);
		List<User> users = userRepository.findAll(pageable).getContent();

		return ResponseEntity.ok(users);
	}

	/**
	 * Get the total number of users
	 *
	 * @return the total number of users
	 */
	@GetMapping("/users/total")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Long> getUserTotal() {
		return ResponseEntity.ok(userRepository.count());
	}

	/**
	 * Get the total number of roles
	 *
	 * @return the total number of roles
	 */
	@GetMapping("/roles/total")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Long> getRolesTotal() {
		return ResponseEntity.ok(roleRepository.count());
	}

	/**
	 * Get a list of all roles
	 *
	 * @return a list of all roles
	 */
	@GetMapping("/roles")
	@Secured(Roles.ADMIN)
	public ResponseEntity<List<Role>> getRoles(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder
	) {
		Sort sort = Sort.unsorted();
		if (sortField != null && !sortField.isEmpty()) {
			Sort.Direction direction = sortOrder < 0 ? Sort.Direction.DESC : Sort.Direction.ASC;
			sort = Sort.by(direction, sortField);
		}

		Pageable pageable = PageRequest.of(page, rows, sort);
		List<Role> roles = roleRepository.findAll(pageable).getContent();

		return ResponseEntity.ok(roles);
	}

	/**
	 * Get a specific role
	 *
	 * @param id the id of the role to get
	 * @return the role with the given id
	 */
	@GetMapping("/roles/{id}")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Role> getRole(@PathVariable String id) {
		return roleRepository
			.findById(Long.parseLong(id))
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Update a role with the given id
	 *
	 * @param id the id of the role to update
	 * @param role the new role data
	 * @return the updated role
	 */
	@PutMapping("/roles/{id}")
	@Secured(Roles.ADMIN)
	public ResponseEntity<Role> updateRole(@PathVariable String id, @RequestBody Role role) {
		return roleRepository
			.findById(Long.parseLong(id))
			.map(roleData -> {
				// The id and name on roles are immutable, the authorities are the only thing that can be changed
				roleData.setAuthorities(role.getAuthorities());
				return ResponseEntity.ok(roleRepository.save(roleData));
			})
			.orElse(ResponseEntity.notFound().build());
	}

	// returns updated user if successful, null otherwise
	@PutMapping("/users/{id}")
	@Secured(Roles.ADMIN)
	public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
		User retUser = null;
		if (adminClientService.updateUserRepresentation(user)) {
			retUser = userRepository.save(user);
		}
		return ResponseEntity.ok(retUser);
	}
}
