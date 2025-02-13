package software.uncharted.terarium.hmiserver.configuration;

import java.util.Arrays;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.JsonResource;
import software.uncharted.terarium.hmiserver.models.authority.User;

@Service
@Primary
public class TestUserDetailsConfiguration implements UserDetailsService {

	@JsonResource("classpath*:/mock-users/*.json")
	User[] USERS;

	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		return Arrays.stream(USERS)
			.filter(user -> user.getUsername().equalsIgnoreCase(username))
			.findFirst()
			.orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
	}
}
