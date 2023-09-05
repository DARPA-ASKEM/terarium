package software.uncharted.terarium.hmiserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.models.User;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getById(final String id) {
    return userRepository.findById(id).orElse(null);
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
