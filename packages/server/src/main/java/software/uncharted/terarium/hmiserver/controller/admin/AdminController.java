package software.uncharted.terarium.hmiserver.controller.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.authority.Group;
import software.uncharted.terarium.hmiserver.models.authority.GroupDTO;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.repository.GroupRepository;
import software.uncharted.terarium.hmiserver.repository.RoleRepository;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.service.GroupService;
import software.uncharted.terarium.hmiserver.service.UserService;
import software.uncharted.terarium.hmiserver.utils.SortedPageRequest;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final GroupRepository groupRepository;
	private final UserService userService;
	private final GroupService groupService;

	/**
	 * Get a list of all users
	 */
	@GetMapping("/users")
	public ResponseEntity<Page<User>> getUsers(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<User> users = userRepository.findByGivenNameContainingOrFamilyNameContainingOrEmailContainingAllIgnoreCase(
			queryString,
			queryString,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);
		return ResponseEntity.ok(users);
	}

	/**
	 * Get a specific user
	 *
	 * @param id the id of the user to get
	 * @return the user with the given id or a 404 if no user exists with that id
	 */
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable String id) {
		return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Get a list of all roles
	 *
	 * @return a list of all roles
	 */
	@GetMapping("/roles")
	public ResponseEntity<Page<Role>> getRoles(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<Role> roles = roleRepository.findByNameContainingOrDescriptionContainingAllIgnoreCase(
			queryString,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);

		return ResponseEntity.ok(roles);
	}

	/**
	 * Find a list of a roles that are not assigned to a user
	 * @param userid  the id of the user to find unassigned roles for
	 * @return  a list of roles that are not assigned to the user
	 */
	@GetMapping("/roles/unassigned/{userid}")
	public ResponseEntity<List<Role>> getUnassignedRoles(@PathVariable String userid) {
		final User user = userRepository.findById(userid).orElseThrow();
		final Set<Long> assignedRoleIds = user.getAllRoles().stream().map(Role::getId).collect(Collectors.toSet());

		return ResponseEntity.ok(roleRepository.findAllByIdNotIn(assignedRoleIds));
	}

	/**
	 * Get a specific role
	 *
	 * @param id the id of the role to get
	 * @return the role with the given id
	 */
	@GetMapping("/roles/{id}")
	public ResponseEntity<Role> getRole(@PathVariable String id) {
		return roleRepository
			.findById(Long.parseLong(id))
			.map(ResponseEntity::ok)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role %s not found", id)));
	}

	/**
	 * Update a role with the given id
	 *
	 * @param role the new role data
	 * @return the updated role
	 */
	@PutMapping("/roles")
	public ResponseEntity<Role> updateRole(@RequestBody Role role) {
		return roleRepository
			.findById(role.getId())
			.map(roleData -> {
				// The id and name on roles are immutable, the authorities are the only thing that can be changed
				roleData.setAuthorities(role.getAuthorities());
				final Role updatedRole = roleRepository.save(roleData);
				log.info("Updated role {} ({})", updatedRole.getName(), updatedRole.getId());
				return ResponseEntity.ok(updatedRole);
			})
			.orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Role %S not found", role.getId()))
			);
	}

	/**
	 * Updates a user
	 * @param user  the new user data
	 * @return      the updated user
	 */
	@PutMapping("/users")
	public ResponseEntity<User> updateUser(@RequestBody User user) {
		final User updatedUser = userService.updateUser(user);
		if (updatedUser == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", user.getId()));
		}
		return ResponseEntity.ok(updatedUser);
	}

	/**
	 * Deletes a user
	 * @param id  the id of the user to delete
	 * @return    success if the user was deleted
	 */
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable String id) {
		final User user = userService.getById(id);
		userService.deleteUser(user);
		return ResponseEntity.ok().build();
	}

	/**
	 * Disables a user
	 * @param id  the id of the user to disable
	 * @return    the updated user
	 */
	@PutMapping("/users/{id}/disable")
	public ResponseEntity<User> disableUser(@PathVariable String id) {
		final User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", id)));
		return ResponseEntity.ok(userService.disable(user));
	}

	/**
	 * Enables a user
	 * @param id  the id of the user to enable
	 * @return    the updated user
	 */
	@PutMapping("/users/{id}/enable")
	public ResponseEntity<User> enableUser(@PathVariable String id) {
		final User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", id)));
		return ResponseEntity.ok(userService.enable(user));
	}

	/**
	 * Forces a user to reset their password on their next login
	 * @param id  the id of the user to reset the password for
	 * @return    success if the operation was successful
	 */
	@PutMapping("/users/{id}/resetpassword")
	public ResponseEntity<Void> resetPassword(@PathVariable String id) {
		final User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", id)));
		userService.resetPassword(user);
		return ResponseEntity.ok(null);
	}

	/**
	 * Removes the given roles from the user
	 * @param id      the id of the user to remove roles from
	 * @param roleIds the ids of the roles to remove
	 * @return        the updated user
	 * @throws ResponseStatusException if the user or any of the roles are not found
	 */
	@PutMapping("/users/{id}/removeroles")
	public ResponseEntity<User> removeRolesFromUser(@PathVariable String id, @RequestBody List<String> roleIds) {
		final User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", id)));
		user.setRoles(
			user.getRoles().stream().filter(role -> !roleIds.contains(role.getId().toString())).collect(Collectors.toSet())
		);
		final User updatedUser = userRepository.save(user);
		log.info("Removed roles {} from user {} ({})", roleIds, updatedUser.getUsername(), updatedUser.getId());
		return ResponseEntity.ok(updatedUser);
	}

	/**
	 * Adds the given roles to the user
	 * @param id      the id of the user to add roles to
	 * @param roleIds the ids of the roles to add
	 * @return      the updated user
	 * @throws ResponseStatusException if the user or any of the roles are not found
	 */
	@PutMapping("/users/{id}/addroles")
	public ResponseEntity<User> addRolesToUser(@PathVariable String id, @RequestBody List<String> roleIds) {
		final User user = userRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("User %s not found", id)));
		final List<Role> roles = roleRepository.findAllById(
			roleIds.stream().map(Long::parseLong).collect(Collectors.toList())
		);
		if (roles.size() != roleIds.size()) {
			final Set<String> missingRoleIds = roleIds
				.stream()
				.filter(roleId -> roles.stream().noneMatch(role -> role.getId().toString().equals(roleId)))
				.collect(Collectors.toSet());
			throw new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				String.format("Roles with ids %s not found", missingRoleIds)
			);
		}

		if (user.getAllRoles().stream().map(Role::getId).map(Object::toString).anyMatch(roleIds::contains)) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				String.format("User %s already has one or more of the roles", id)
			);
		}

		final Set<Role> userRoles = new HashSet<>(user.getRoles());
		userRoles.addAll(roles);
		user.setRoles(userRoles);
		final User updatedUser = userRepository.save(user);
		log.info("Added roles {} to user {} ({})", roleIds, updatedUser.getUsername(), updatedUser.getId());
		return ResponseEntity.ok(updatedUser);
	}

	/**
	 * Creates a new group
	 * @param model the group to create
	 * @return      the created group
	 */
	@PostMapping("/group")
	public ResponseEntity<Group> createGroup(@RequestBody GroupDTO model) {
		if (model.getName() == null || model.getName().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
		}
		if (model.getDescription() == null || model.getDescription().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group description is required");
		}

		final Group group = groupService.createGroup(model.getName(), model.getDescription(), null);
		return ResponseEntity.ok(group);
	}

	/**
	 * Get a list of all groups
	 *
	 * @return a list of all groups
	 */
	@GetMapping("/groups")
	public ResponseEntity<Page<Group>> getGroups(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<Group> groups = groupRepository.findByNameContainingOrDescriptionContainingAllIgnoreCase(
			queryString,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);

		return ResponseEntity.ok(groups);
	}

	/**
	 * Gets the default groups that users are automatically added to when they are created
	 * @return  a list of default group ids
	 */
	@GetMapping("/groups/default")
	public ResponseEntity<List<String>> getDefaultGroups() {
		return ResponseEntity.ok(groupService.getDefaultGroups().stream().map(Group::getName).collect(Collectors.toList()));
	}

	/**
	 * Gets a group by its id
	 * @param id  the id of the group to get
	 * @return    the {@link Group} with the given id or a 404 if no group exists with that id
	 */
	@GetMapping("/group/{id}")
	public ResponseEntity<Group> getGroup(@PathVariable String id) {
		return groupRepository
			.findById(id)
			.map(ResponseEntity::ok)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group %s not found", id)));
	}

	/**
	 * Returns a page of group members for the given group and search parameters
	 * @param id            the id of the group to get members for
	 * @param page          the page number to get
	 * @param rows          the number of rows per page
	 * @param sortField     the field to sort by
	 * @param sortOrder     the sort order (1 for ascending, -1 for descending)
	 * @param queryString   a string to filter the results by
	 * @return              a page of group members
	 */
	@GetMapping("/group/{id}/members")
	public ResponseEntity<Page<User>> getGroupMembers(
		@PathVariable String id,
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<User> members = userRepository.findGroupMembers(
			Set.of(id),
			queryString,
			queryString,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);

		return ResponseEntity.ok(members);
	}

	/**
	 * Like {@link #getGroupMembers(String, Integer, Integer, String, Integer, String)} but returns a page of non-members.
	 * This is useful for determing what users are not in a group and can be added.
	 * @param id            the id of the group to get non-members for
	 * @param page          the page number to get
	 * @param rows          the number of rows per page
	 * @param sortField     the field to sort by
	 * @param sortOrder     the sort order (1 for ascending, -1 for descending)
	 * @param queryString   a string to filter the results by
	 * @return              a page of non-members
	 */
	@GetMapping("/group/{id}/non-members")
	public ResponseEntity<Page<User>> getGroupNonMembers(
		@PathVariable String id,
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<User> members = userRepository.findGroupNonMembers(
			Set.of(id),
			queryString,
			queryString,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);

		return ResponseEntity.ok(members);
	}

	/**
	 * Returns a page of roles that are not assigned to the given group. This is useful for determining what
	 * roles can be added to a group.
	 * @param id            the id of the group to get unassigned roles for
	 * @param page          the page number to get
	 * @param rows          the number of rows per page
	 * @param sortField     the field to sort by
	 * @param sortOrder     the sort order (1 for ascending, -1 for descending)
	 * @param queryString   a string to filter the results by
	 * @return              a page of unassigned roles
	 */
	@GetMapping("/group/{id}/roles/unassigned")
	public ResponseEntity<Page<Role>> getUnassignedGroupRoles(
		@PathVariable String id,
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false) String sortField,
		@RequestParam(required = false) Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<Role> roles = groupRepository.findUnassignedRoles(
			id,
			queryString,
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);

		return ResponseEntity.ok(roles);
	}

	/**
	 * Add users to a group
	 * @param id      the id of the group to update
	 * @param userIds the ids of the users to add
	 * @return        the updated group
	 * @throws ResponseStatusException if the group or any of the users are not found
	 */
	@PutMapping("/group/{id}/users/add")
	public ResponseEntity<Void> addToGroup(@PathVariable String id, @RequestBody List<String> userIds) {
		final Group group = groupRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group %s not found", id)));

		groupService.addUsersToGroupByUserIds(Set.of(group.getId()), userIds);

		return ResponseEntity.ok().build();
	}

	/**
	 * Remove users from a group
	 * @param id      the id of the group to update
	 * @param userIds the ids of the users to remove
	 * @return        the updated group
	 * @throws ResponseStatusException if the group or any of the users are not found
	 */
	@PutMapping("/group/{id}/users/remove")
	public ResponseEntity<Void> removeFromGroup(@PathVariable String id, @RequestBody List<String> userIds) {
		final Group group = groupRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group %s not found", id)));

		groupService.removeUsersFromGroupByUserIds(Set.of(group.getId()), userIds);

		return ResponseEntity.ok().build();
	}

	/**
	 * Add roles to a group
	 * @param id      the id of the group to update
	 * @param roleIds the ids of the roles to add
	 * @return        the updated group
	 * @throws ResponseStatusException if the group or any of the roles are not found
	 */
	@PutMapping("/group/{id}/roles/add")
	public ResponseEntity<Group> addRolesToGroup(@PathVariable String id, @RequestBody List<String> roleIds) {
		final Group group = groupRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group %s not found", id)));
		final List<Role> roles = roleRepository.findAllById(
			roleIds.stream().map(Long::parseLong).collect(Collectors.toList())
		);
		if (roles.size() != roleIds.size()) {
			final Set<String> missingRoleIds = roleIds
				.stream()
				.filter(roleId -> roles.stream().noneMatch(role -> role.getId().toString().equals(roleId)))
				.collect(Collectors.toSet());
			throw new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				String.format("Roles with ids %s not found", missingRoleIds)
			);
		}

		if (group.getRoles().stream().map(Role::getId).map(Object::toString).anyMatch(roleIds::contains)) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				String.format("Group %s already has one or more of the roles", id)
			);
		}

		return ResponseEntity.ok(groupService.addRoles(group, roles));
	}

	/**
	 * Remove roles from a group
	 * @param id        the id of the group to update
	 * @param roleIds   the ids of the roles to remove
	 * @return          the updated group
	 * @throws ResponseStatusException if the group or any of the roles are not found
	 */
	@PutMapping("/group/{id}/roles/remove")
	public ResponseEntity<Group> removeRolesFromGroup(@PathVariable String id, @RequestBody List<String> roleIds) {
		final Group group = groupRepository
			.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group %s not found", id)));
		final List<Role> roles = roleRepository.findAllById(
			roleIds.stream().map(Long::parseLong).collect(Collectors.toList())
		);
		if (roles.size() != roleIds.size()) {
			final Set<String> missingRoleIds = roleIds
				.stream()
				.filter(roleId -> roles.stream().noneMatch(role -> role.getId().toString().equals(roleId)))
				.collect(Collectors.toSet());
			throw new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				String.format("Roles with ids %s not found", missingRoleIds)
			);
		}

		return ResponseEntity.ok(groupService.removeRoles(group, roles));
	}

	/**
	 * Get a list of all audit events for viewing in the GenericTable component
	 * @param page          the page number to get
	 * @param rows          the number of rows per page
	 * @param sortField     the field to sort by, defaults to timestampMs
	 * @param sortOrder     the sort order, defaults to -1 (descending)
	 * @param queryString   a string to filter the results by. The query string will be matched using an {@link AuditEventSpecification}
	 *                      which will match the query string against the following fields:
	 *                      <ul>
	 *                        <li>user.id</li>
	 *                        <li>user.givenName</li>
	 *                        <li>user.familyName</li>
	 *                        <li>project.id</li>
	 *                        <li>project.name</li>
	 *                        <li>data</li>
	 *                      </ul>
	 * @return              a page of {@link AuditEvent}s
	 */
	@GetMapping("/events")
	public ResponseEntity<Page<AuditEvent>> getEvents(
		@RequestParam(required = false, defaultValue = "0") Integer page,
		@RequestParam(required = false, defaultValue = "5") Integer rows,
		@RequestParam(required = false, defaultValue = "timestampMs") String sortField,
		@RequestParam(required = false, defaultValue = "-1") Integer sortOrder,
		@RequestParam(required = false, defaultValue = "") String queryString
	) {
		Page<AuditEvent> events = auditEventRepository.findAll(
			AuditEventSpecification.filterBy(queryString),
			SortedPageRequest.of(sortField, sortOrder, page, rows)
		);
		return ResponseEntity.ok(events);
	}
}
