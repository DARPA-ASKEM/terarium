package software.uncharted.terarium.hmiserver.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@TSModel
@MappedSuperclass
public abstract class TerariumAsset implements Serializable {

    @Id
    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id = UUID.randomUUID();

    @TSOptional
    private String name;

    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp createdOn;

    // Don't use @CreationTimestamp because it doesn't get populated until after the
    // transaction is committed.
    @PrePersist
    protected void onCreate() {
        this.createdOn =
                Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
    }

    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp updatedOn;

    // Don't use @UpdateTimestamp because it doesn't get populated until after the
    // transaction is committed.
    @PreUpdate
    protected void onUpdate() {
        this.updatedOn =
                Timestamp.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
    }

    @TSOptional
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Timestamp deletedOn;

    @TSOptional
    private Boolean temporary = false;

    @TSOptional
    private Boolean publicAsset = false;

    // This is here just to satisfy the service interface.
    public TerariumAsset clone() {
        throw new RuntimeException(
                "This should not be called. Override this method on the derived class and call cloneSuperFields instead.");
    }

    protected TerariumAsset cloneSuperFields(final TerariumAsset asset) {
        asset.id = UUID.randomUUID(); // ensure we create a new id
        asset.name = name;
        asset.createdOn = this.createdOn != null ? new Timestamp(this.createdOn.getTime()) : null;
        asset.updatedOn = this.updatedOn != null ? new Timestamp(this.updatedOn.getTime()) : null;
        asset.deletedOn = this.deletedOn != null ? new Timestamp(this.deletedOn.getTime()) : null;
        asset.temporary = temporary;
        asset.publicAsset = publicAsset;
        return asset;
    }
}
