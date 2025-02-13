package software.uncharted.terarium.hmiserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurrentUserService {

	private final UserService userService;
	private final AdminClientService adminClientService;

	public static Jwt getToken() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (Jwt) (authentication.getPrincipal());
	}

	public User get() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication.getPrincipal() instanceof User) {
			return (User) authentication.getPrincipal();
		} else if (authentication.getPrincipal() instanceof final org.springframework.security.core.userdetails.User u) {
			// Used in tests
			final User user = new User();
			final List<SimpleGrantedAuthority> auths = new ArrayList<>();
			for (final GrantedAuthority auth : u.getAuthorities()) {
				auths.add(new SimpleGrantedAuthority(auth.getAuthority()));
			}
			user.setUsername(u.getUsername());
			user.setAuthorities(auths);
			return user;
		} else {
			final Jwt jwt = (Jwt) (authentication.getPrincipal());
			final User user = adminClientService
				.getUserFromJwt(jwt)
				.setAuthorities(
					authentication
						.getAuthorities()
						.stream()
						.map(a -> new SimpleGrantedAuthority(a.getAuthority()))
						.collect(Collectors.toList())
				);

			final User storedUser = userService.getById(user.getId());
			user.merge(storedUser);
			return user;
		}
	}
}
