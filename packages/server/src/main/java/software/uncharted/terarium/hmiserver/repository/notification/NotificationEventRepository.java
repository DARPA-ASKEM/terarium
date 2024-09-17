package software.uncharted.terarium.hmiserver.repository.notification;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface NotificationEventRepository extends PSCrudRepository<NotificationEvent, UUID> {
	@Modifying
	@Query(
		"UPDATE NotificationEvent e SET e.acknowledgedOn = :acknowledgedOn WHERE e.notificationGroup.id = :notificationGroupId"
	)
	void setAcknowledgedOnWhereNotificationGroupIdEquals(
		@Param("notificationGroupId") UUID notificationGroupId,
		@Param("acknowledgedOn") Timestamp acknowledgedOn
	);
}
