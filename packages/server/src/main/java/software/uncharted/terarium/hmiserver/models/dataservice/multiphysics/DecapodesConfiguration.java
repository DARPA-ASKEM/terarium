package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class DecapodesConfiguration extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -4918948630082610185L;

	private ConfigurationHeader header;

	private Configuration configuration;
}
