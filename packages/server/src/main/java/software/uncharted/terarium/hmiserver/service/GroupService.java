package software.uncharted.terarium.hmiserver.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.authority.Role;
import software.uncharted.terarium.hmiserver.models.authority.RoleType;
import software.uncharted.terarium.hmiserver.repository.GroupRepository;
import software.uncharted.terarium.hmiserver.repository.ProjectGroupPermissionRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

	private final GroupRepository groupRepository;
	private final UserService userService;
	private final ProjectGroupPermissionRepository projectGroupPermissionRepository;

	@Getter
	private static final Map<String, Set<RoleType>> defaultGroupDefinitions = Map.of("Everyone", Set.of(RoleType.USER));

	@Getter
	private static final Map<String, String> defaultGroupDescriptions = Map.of("Everyone", "Default group for all users");

	/**
	 * Get the default groups
	 * @return  The default groups
	 */
	public Collection<Group> getDefaultGroups() {
		return groupRepository.findAllByNameIn(getDefaultGroupNames());
	}

	/**
	 * Get the default group names
	 * @return  The default group names
	 */
	public static Collection<String> getDefaultGroupNames() {
		return defaultGroupDefinitions.keySet();
	}

	/**
	 * Get the total number of groups
	 * @return  The total number of groups
	 */
	public Long count() {
		return groupRepository.count();
	}

	/**
	 * Get a group by its id
	 * @param id  The group id
	 * @return    The group, or null if it does not exist
	 */
	public Group getGroup(final String id) {
		return groupRepository.findById(id).orElse(null);
	}

	/**
	 * Get a group by its name (names are unique)
	 * @param name  The group name
	 * @return      The group, or null if it does not exist
	 */
	public Group getGroupByName(final String name) {
		return groupRepository.findByName(name);
	}

	/**
	 * Get a collection of groups by their ids
	 * @param ids   The group ids
	 * @return      The groups
	 */
	public Collection<Group> getGroups(final Collection<String> ids) {
		return groupRepository.findAllById(ids);
	}

	/**
	 * Stream users by a group
	 * @param id    the group id
	 * @return      A stream of users
	 */
	public Stream<User> streamByGroup(final String id) {
		final Group group = groupRepository.findByName(id);
		return userService.streamByGroup(group);
	}

	/**
	 * Get a group by its name
	 * @param name        The group name to create
	 * @param description The description of the group
	 * @param roles       The role associated with the group
	 * @return            The created group
	 */
	@Transactional
	public Group createGroup(final String name, final String description, final Collection<Role> roles) {
		final Group group = new Group()
			.setCreatedAtMs(Instant.now().toEpochMilli())
			.setDescription(description)
			.setName(name)
			.setRoles(roles);
		final Group createdGroup = groupRepository.save(group);
		log.info("Created group {} ({})", createdGroup.getName(), createdGroup.getId());
		return createdGroup;
	}

	@Transactional
	public Group addRoles(final Group group, final Collection<Role> roles) {
		Collection<Role> existingRoles = group.getRoles();
		if (existingRoles == null) {
			existingRoles = new HashSet<>();
		}
		existingRoles.addAll(roles);
		group.setRoles(existingRoles);

		final Group updatedGroup = groupRepository.save(group);
		log.info("Added roles {} to group {} ({})", roles, updatedGroup.getName(), updatedGroup.getId());
		return updatedGroup;
	}

	@Transactional
	public Group removeRoles(final Group group, final Collection<Role> roles) {
		Collection<Role> existingRoles = group.getRoles();
		if (existingRoles == null) {
			return group;
		}
		existingRoles.removeAll(roles);
		group.setRoles(existingRoles);

		final Group updatedGroup = groupRepository.save(group);
		log.info("Removed roles {} from group {} ({})", roles, updatedGroup.getName(), updatedGroup.getId());
		return updatedGroup;
	}

	@Transactional
	public void addUsersToGroups(final Collection<Group> groups, final Collection<User> users) {
		addUserGroups(groups, users);
	}

	/**
	 * Add a collection of users to a group
	 * @param groupIds    The group ids to add
	 * @param users       The users to add
	 */
	@Transactional
	public void addUsersToGroupByGroupIds(final Collection<String> groupIds, final Collection<User> users) {
		final List<Group> groups = groupRepository.findAllById(groupIds);
		addUserGroups(groups, users);
	}

	/**
	 * Add a collection of users to a group by their ids
	 * @param groupIds    The group ids
	 * @param userIds     The user ids
	 */
	@Transactional
	public void addUsersToGroupByUserIds(final Collection<String> groupIds, final Collection<String> userIds) {
		final List<Group> groups = groupRepository.findAllById(groupIds);

		try (Stream<User> userStream = userService.streamByIds(userIds)) {
			final List<User> batch = new ArrayList<>();
			userStream.forEach(user -> {
				batch.add(user);
				if (batch.size() >= 100) {
					addUserGroups(groups, batch);
					batch.clear();
				}
			});
			if (!batch.isEmpty()) {
				addUserGroups(groups, batch);
			}
		}
	}

	/**
	 * Remove a collection of users from groups
	 * @param groups  The groups to remove the users from
	 * @param users   The users to remove from the groups
	 */
	@Transactional
	public void removeUsersFromGroups(final Collection<Group> groups, final Collection<User> users) {
		removeUserGroups(groups, users);
	}

	/**
	 * Remove a collection of users from groups by their group ids
	 * @param groupIds    The group ids
	 * @param users       The users to remove from the groups
	 */
	@Transactional
	public void removeUsersFromGroupByGroupIds(final Collection<String> groupIds, final Collection<User> users) {
		final List<Group> groups = groupRepository.findAllById(groupIds);
		removeUserGroups(groups, users);
	}

	/**
	 * Remove a collection of users from groups by their user ids
	 * @param groupIds    The group ids
	 * @param userIds     The user ids
	 */
	@Transactional
	public void removeUsersFromGroupByUserIds(final Collection<String> groupIds, final Collection<String> userIds) {
		final List<Group> groups = groupRepository.findAllById(groupIds);

		try (Stream<User> userStream = userService.streamByIds(userIds)) {
			final List<User> batch = new ArrayList<>();
			userStream.forEach(user -> {
				batch.add(user);
				if (batch.size() >= 100) {
					removeUserGroups(groups, batch);
					batch.clear();
				}
			});
			if (!batch.isEmpty()) {
				removeUserGroups(groups, batch);
			}
		}
	}

	/**
	 * Update the user groups for a collection of users
	 * @param groups  The group to add the users to
	 * @param users   The users to add to the group
	 */
	private void addUserGroups(Collection<Group> groups, Collection<User> users) {
		final Collection<User> usersToSave = new ArrayList<>();
		users.forEach(user -> {
			Collection<Group> existingGroups = user.getGroups();
			if (existingGroups == null) {
				existingGroups = new HashSet<>();
			}
			existingGroups.addAll(groups);
			user.setGroups(existingGroups);
			usersToSave.add(user);
		});
		userService.saveAll(usersToSave);
		log.info(
			"Added users {} to groups {}",
			users.stream().map(User::getUsername).toList(),
			groups.stream().map(Group::getName).toList()
		);
	}

	/**
	 * Remove the groups from the collection of users
	 * @param groups  The groups to remove
	 * @param users   The users to remove the groups from
	 */
	private void removeUserGroups(Collection<Group> groups, Collection<User> users) {
		final Collection<User> usersToSave = new ArrayList<>();
		users.forEach(user -> {
			final Collection<Group> existingGroups = user.getGroups();
			if (existingGroups == null) {
				return;
			}
			existingGroups.removeAll(groups);
			user.setGroups(existingGroups);
			usersToSave.add(user);
		});

		userService.saveAll(usersToSave);
		log.info(
			"Removed users {} from groups {}",
			users.stream().map(User::getUsername).toList(),
			groups.stream().map(Group::getName).toList()
		);
	}

	/**
	 * Delete a group by its id
	 * @param id  The group id
	 */
	@Transactional
	public void deleteGroup(final String id) {
		final Group group = getGroup(id);
		groupRepository.delete(group);
		projectGroupPermissionRepository.deleteByGroupId(id);
		log.info("Deleted group {} ({})", group.getName(), group.getId());
	}
}
