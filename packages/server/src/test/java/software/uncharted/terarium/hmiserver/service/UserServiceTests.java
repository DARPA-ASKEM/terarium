package software.uncharted.terarium.hmiserver.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests extends TerariumApplicationTests {

	@Autowired
	UserService userService;

	@MockBean
	UserRepository mockRepository;

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testItCachesUsers() {
		final String targetId = MockUser.ADAM;
		final User mockUser = new User()
			.setId(MockUser.ADAM)
			.setGivenName("Emperor")
			.setFamilyName("Adam")
			.setEmail("adam@test.io");

		// Tell the repository function to return the mocked user
		Mockito.when(mockRepository.findById(targetId)).thenReturn(Optional.ofNullable(mockUser));

		// Call the user service twice
		userService.getById(targetId);
		userService.getById(targetId);

		// Verify that the database was only accessed once as the result should be cached
		Mockito.verify(mockRepository, Mockito.atMostOnce()).findById(targetId);
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testItCreatesAndCachesUsers() {
		final String targetId = MockUser.ADAM;
		final User mockUser = new User()
			.setId(MockUser.ADAM)
			.setGivenName("Emperor")
			.setFamilyName("Adam")
			.setEmail("adam@test.io");

		// Tell the repository function to return the mocked user
		Mockito.when(mockRepository.findById(targetId)).thenReturn(Optional.ofNullable(mockUser));

		// Create user
		UserService.createUser(mockUser);

		// Call the user service twice
		userService.getById(targetId);
		userService.getById(targetId);

		// Verify that the database was only accessed once as the result should be cached
		Mockito.verify(mockRepository, Mockito.atMostOnce()).findById(targetId);
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testItCreatesAndUpdateAndCachesUsers() {
		final String targetId = MockUser.ADAM;
		final User mockUser = new User()
			.setId(MockUser.ADAM)
			.setGivenName("Emperor")
			.setFamilyName("Adam")
			.setEmail("adam@test.io");

		// Tell the repository function to return the mocked user
		Mockito.when(mockRepository.findById(targetId)).thenReturn(Optional.ofNullable(mockUser));

		// Create user
		UserService.createUser(mockUser);

		// Update user
		mockUser.setGivenName("King");
		userService.save(mockUser);

		// Call the user service twice
		userService.getById(targetId);
		userService.getById(targetId);

		// Verify that the database was only accessed once as the result should be cached
		Mockito.verify(mockRepository, Mockito.atMostOnce()).findById(targetId);
	}
}
