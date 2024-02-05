package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesConfiguration extends TerariumAsset implements Serializable {
	private ConfigurationHeader header;
	private Configuration configuration;
}
