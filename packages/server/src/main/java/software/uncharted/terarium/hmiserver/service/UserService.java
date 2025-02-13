package software.uncharted.terarium.hmiserver.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.repository.GroupRepository;
import software.uncharted.terarium.hmiserver.repository.UserRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final AdminClientService adminClientService;
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final S3ClientService s3ClientService;

	@Value("${pantera.avatar-s3-bucket-name:pantera-core-avatars}")
	private String AVATAR_BUCKET_NAME;

	@Value("${pantera.avatar-s3-client-name:default}")
	private String AVATAR_S3_CLIENT_NAME;

	@PostConstruct
	void init() {
		// Create the avatar bucket if it doesn't exist
		final S3Service s3Service = s3ClientService.getS3Service(AVATAR_S3_CLIENT_NAME);
		if (!s3Service.bucketExists(AVATAR_BUCKET_NAME)) {
			s3Service.createBucket(AVATAR_BUCKET_NAME);
		}
	}

	/**
	 * Get a user by their user id
	 * @param id  The user id
	 * @return    The user
	 */
	public User getById(final String id) {
		return userRepository.findById(id).orElse(null);
	}

	/**
	 * Get a collection of users by their user ids
	 * @param ids The user ids
	 * @return    The users
	 */
	public Collection<User> getByIds(final Collection<String> ids) {
		return userRepository.findAllById(ids);
	}

	/**
	 * Get a user by their username
	 * @param username  The username
	 * @return          The user
	 */
	public User getByUsername(final String username) {
		return userRepository.findByUsername(username).orElse(null);
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
	 * Stream users by a group
	 * @param group the group
	 * @return      A stream of users
	 */
	public Stream<User> streamByGroup(final Group group) {
		return userRepository.readAllByGroupsContains(group);
	}

	/**
	 * Creates a new user
	 * @param user  The user model to create
	 * @return      The created user
	 */
	@Transactional
	public User createUser(User user) {
		final long now = Instant.now().toEpochMilli();
		user.setCreatedAtMs(now);
		user.setLastLoginAtMs(now);
		user.setLastUpdateAtMs(now);
		final Collection<Group> defaultGroups = groupRepository.findAllByNameIn(GroupService.getDefaultGroupNames());
		if (user.getGroups() != null) {
			final Set<Group> groups = new HashSet<>();
			groups.addAll(user.getGroups());
			groups.addAll(defaultGroups);
			user.setGroups(groups);
		} else {
			user.setGroups(new HashSet<>(defaultGroups));
		}

		auditEventService.createEvent(
			null,
			AuditEventType.USER_CREATE,
			null,
			new NamedEntity(user.getId(), user.getUsername())
		);

		return save(user);
	}

	/**
	 * Updates a user
	 * @param user  The user model to update
	 * @return      The updated user
	 */
	@Transactional
	public User updateUser(User user) {
		if (adminClientService.updateUserRepresentation(user)) {
			// Fetch existing user
			User existingUser = userRepository.findById(user.getId()).orElse(null);
			if (existingUser != null) {
				// Update fields
				existingUser.setEmail(user.getEmail());
				existingUser.setGivenName(user.getGivenName());
				existingUser.setFamilyName(user.getFamilyName());
				auditEventService.createEvent(
					AuditEventType.USER_UPDATE,
					new NamedEntity(existingUser.getId(), existingUser.getUsername())
				);
				return save(existingUser);
			}
		}
		return null;
	}

	/**
	 * Deletes a user
	 * @param user  The user model to delete
	 */
	@Transactional
	public void deleteUser(User user) {
		final String id = user.getId();
		final String username = user.getUsername();
		auditEventService.createEvent(AuditEventType.USER_DELETE, new NamedEntity(id, username));
		userRepository.deleteById(user.getId());
		adminClientService.deleteUser(user);
		log.info("Deleted user {}", username);
	}

	/**
	 * Saves a user. Ensures that the last update time is set
	 * @param user  The user to save
	 * @return      The saved user
	 */
	public User save(User user) {
		user.setLastUpdateAtMs(Instant.now().toEpochMilli());
		final User persistedUser = userRepository.save(user);
		log.info("Saved user {} ({})", persistedUser.getUsername(), persistedUser.getId());
		return persistedUser;
	}

	/**
	 * Saves a collection of users. Ensures that the last update time is set
	 * @param users The users to save
	 * @return      The saved users
	 */
	public List<User> saveAll(Collection<User> users) {
		final long now = Instant.now().toEpochMilli();
		users.forEach(user -> user.setLastUpdateAtMs(now));
		return userRepository.saveAll(users);
	}

	/**
	 * Saves the avatar for a user. The avatar is saved to S3 and the user's hasAvatar flag is set to true
	 * @param userId  The user ID
	 * @param bytes   The avatar image as a byte array
	 */
	public void saveAvatar(final String userId, final byte[] bytes) {
		try {
			// Read image
			BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(bytes));

			// Square off the image, filling with blackspace
			int width = thumbnail.getWidth();
			int height = thumbnail.getHeight();
			if (width > height) {
				thumbnail = Scalr.crop(thumbnail, (width - height) / 2, 0, height, height);
			} else if (width < height) {
				thumbnail = Scalr.crop(thumbnail, 0, (height - width) / 2, width, width);
			}

			// Scale down to 256x256
			thumbnail = Scalr.resize(thumbnail, 256);

			// Write to png
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(thumbnail, "png", baos);
			final byte[] pngBytes = baos.toByteArray();

			final S3Service s3Service = s3ClientService.getS3Service(AVATAR_S3_CLIENT_NAME);
			s3Service.putObject(AVATAR_BUCKET_NAME, userId + ".png", pngBytes);
			userRepository
				.findById(userId)
				.ifPresent(user -> {
					user.setHasAvatar(true);
					final User updatedUser = userRepository.save(user);
					log.info("Saved avatar for user {}", updatedUser.getUsername());
				});
		} catch (final IOException e) {
			log.error("Error saving avatar", e);
		}
	}

	/**
	 * Get the avatar for a user
	 * @param userId  The user ID
	 * @return        The avatar image as a PNG byte array
	 * @throws IOException  If the avatar could not be read
	 */
	public byte[] getAvatar(final String userId) throws IOException {
		final S3Service s3Service = s3ClientService.getS3Service(AVATAR_S3_CLIENT_NAME);
		return s3Service.getObject(AVATAR_BUCKET_NAME, userId + ".png").readAllBytes();
	}

	/**
	 * Enables the given user
	 * @param user  The user to enable
	 * @return      The enabled user
	 */
	public User enable(User user) {
		user.setEnabled(true);
		adminClientService.enable(user);
		final User updated = userRepository.save(user);
		log.info("Enabled user {}", updated.getUsername());
		return updated;
	}

	/**
	 * Disables the given user
	 * @param user  The user to disable
	 * @return      The disabled user
	 */
	public User disable(User user) {
		user.setEnabled(false);
		adminClientService.disable(user);
		final User updated = userRepository.save(user);
		log.info("Disabled user {}", updated.getUsername());
		return updated;
	}

	/**
	 * Forces a password reset for the given user
	 * @param user  The user to reset the password for
	 */
	public void resetPassword(User user) {
		adminClientService.resetPassword(user);
	}
}
