package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesConfiguration implements Serializable {
	private ConfigurationHeader header;
	private Configuration configuration;
}
