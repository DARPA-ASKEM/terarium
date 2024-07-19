package software.uncharted.terarium.hmiserver.repository.notification;

import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface NotificationGroupRepository extends PSCrudRepository<NotificationGroup, UUID> {
	List<NotificationGroup> findAllByUserIdOrderByCreatedOnDesc(@NotNull String userId);

	List<NotificationGroup> findAllByUserIdAndCreatedOnGreaterThanOrderByCreatedOnDesc(
		@NotNull String userId,
		@NotNull Timestamp createdOn
	);

	@Query(
		"""
		SELECT ng
		FROM NotificationGroup ng
		JOIN ng.notificationEvents ne ON ne.notificationGroup.id = ng.id
		WHERE ng.userId = :userId
		AND ng.createdOn > :createdOn
		AND (SELECT COUNT(ne2)
			FROM NotificationEvent ne2
			WHERE ne2.notificationGroup.id = ng.id
			AND ne2.acknowledgedOn IS NULL) > 0
		ORDER BY ng.createdOn DESC
		"""
	)
	List<NotificationGroup> findAllUnackedByUserReturnAllEvents(
		@Param("userId") @NotNull String userId,
		@Param("createdOn") @NotNull Timestamp createdOn
	);
}
