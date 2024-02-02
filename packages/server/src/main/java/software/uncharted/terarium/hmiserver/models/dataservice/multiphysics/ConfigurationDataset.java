package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import java.util.List;
import java.lang.Number;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import lombok.experimental.Accessors;
import java.io.Serializable;


@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationDataset implements Serializable {
	private String type;
	private String name;
	private String description;

	private ConfigurationDatasetFile file;
}
