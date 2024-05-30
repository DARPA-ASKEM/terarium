package software.uncharted.terarium.hmiserver.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
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
}
