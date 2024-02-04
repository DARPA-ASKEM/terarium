package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;

import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesContext extends TerariumAsset implements Serializable {
	private ContextHeader header;
	private Context context;
}
