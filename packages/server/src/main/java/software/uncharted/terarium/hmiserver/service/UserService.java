package software.uncharted.terarium.hmiserver.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;

	@Cacheable(value = "users", key = "#id", unless = "#result == null")
	public User getById(final String id) {
		final User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			log.warn("User not found for id: {}", id);
		}
		return user;
	}

	public static User createUser(final User user) {
		final long now = Instant.now().toEpochMilli();
		// using milliseconds causes User.isDirty() to be true for every single call the user makes, which in turn
		// results in 3 SQL calls to update the User record.
		//    UPDATE user SET <all fields>
		//    DELETE users_roles WHERE user_id=<id>
		//    INSERT users_roles
		final long nowInDays = TimeUnit.MILLISECONDS.toDays(now);
		user.setCreatedAtMs(now);
		user.setLastLoginAtMs(TimeUnit.DAYS.toMillis(nowInDays));
		return user;
	}

	@CachePut(value = "users", key = "#user.id")
	public User save(final User user) {
		return userRepository.save(user);
	}

	/**
	 * Stream users by a group
	 * @param group the group
	 * @return      A stream of users
	 */
	public Stream<User> streamByGroup(final Group group) {
		return userRepository.readAllByGroupsContains(group);
	}

	/**
	 * Stream users by a set of ids
	 * @param ids    The full set of user ids to load
	 * @return       A stream of users
	 */
	public Stream<User> streamByIds(final Collection<String> ids) {
		return userRepository.readAllByIdIn(ids);
	}

	/**
	 * Saves a collection of users. Ensures that the last update time is set
	 * @param users The users to save
	 * @return      The saved users
	 */
	public List<User> saveAll(Collection<User> users) {
		return userRepository.saveAll(users);
	}
}
