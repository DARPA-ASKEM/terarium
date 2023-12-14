package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;

	public User getById(final String id) {
		final User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			log.warn("User not found for id: {}", id);
		}
		return user;
	}

	public User createUser(final User user) {
		final long now = Instant.now().toEpochMilli();
		user.setCreatedAtMs(now);
		user.setLastLoginAtMs(now);
		return save(user);
	}

	public User save(final User user) {
		return userRepository.save(user);
	}
}
