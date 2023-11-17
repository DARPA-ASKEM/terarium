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
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			log.error("User not found for id: {}", id);
		}
		return user;
	}

	public User createUser(User user) {
		final long now = Instant.now().toEpochMilli();
		user.setCreatedAtMs(now);
		user.setLastLoginAtMs(now);
		return save(user);
	}

	public User save(User user) {
		return userRepository.save(user);
	}
}
