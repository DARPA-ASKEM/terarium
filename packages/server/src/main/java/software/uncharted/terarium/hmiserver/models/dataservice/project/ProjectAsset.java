package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class ProjectAsset extends TerariumAsset implements Serializable {

	@Serial
	private static final long serialVersionUID = -3382397588627700379L;


	@ManyToOne
	@JoinColumn(name = "project_id", nullable = false)
	@JsonBackReference
	@NotNull
	private Project project;

	@NotNull
	private UUID assetId;

	@NotNull
	@Enumerated(EnumType.STRING)
	private AssetType assetType;

	@NotNull
	private String assetName;

	@TSOptional
	private String externalRef;

}
