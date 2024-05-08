package software.uncharted.terarium.hmiserver.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.sql.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TSModel
@MappedSuperclass
public abstract class TerariumAsset extends TerariumEntity {

	@TSOptional
	@Column(length = 512)
	@Schema(defaultValue = "Default Name")
	private String name;

	@TSOptional
	@Schema(defaultValue = "Default Description")
	@Column(columnDefinition = "text")
	private String description;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

	@TSOptional
	private Boolean temporary = false;

	@TSOptional
	private Boolean publicAsset = false;

	// This is here just to satisfy the service interface.
	@Override
	public TerariumAsset clone() {
		throw new RuntimeException(
				"This should not be called. Override this method on the derived class and call cloneSuperFields instead.");
	}

	protected TerariumAsset cloneSuperFields(final TerariumAsset asset) {

		super.cloneSuperFields(asset);

		asset.name = name;
		asset.description = description;
		asset.deletedOn = this.deletedOn != null ? new Timestamp(this.deletedOn.getTime()) : null;
		asset.temporary = temporary;
		asset.publicAsset = publicAsset;

		return asset;
	}
}
