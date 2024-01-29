package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.EqualsAndHashCode;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Accessors(chain = true)
@TSModel
public class ModelFramework extends TerariumAsset implements SupportAdditionalProperties, Serializable {

	@Serial
	private static final long serialVersionUID = -2375250471839320328L;


	private String name;
	private String version;
	private String semantics;
}
