package software.uncharted.terarium.hmiserver.repository.notification;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface NotificationEventRepository extends PSCrudRepository<NotificationEvent, UUID> {}
