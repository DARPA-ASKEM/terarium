package software.uncharted.terarium.hmiserver.models.notification;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
@Entity
public class NotificationGroup {

    @Serial
    private static final long serialVersionUID = -3382397588627700379L;

    @Id
    private UUID id = UUID.randomUUID();

    @NotNull private String userId;

    @NotNull private String type;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp createdOn;

    @OneToMany(mappedBy = "notificationGroup", fetch = FetchType.EAGER)
    @OrderBy("createdOn DESC")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @ToString.Exclude
    @JsonManagedReference
    private List<NotificationEvent> notificationEvents;

    @PrePersist
    protected void onCreate() {
        this.createdOn = this.createdOn != null
                ? this.createdOn
                : Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
    }
}
