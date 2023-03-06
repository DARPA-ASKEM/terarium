package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Dataset extends ResourceType implements Serializable {

	private String name;

	private String url;

	private String description;

	private LocalDateTime timestamp;

	private Boolean deprecated;

	private String sensitivity;

	private String quality;

	@JsonAlias("temporal_resolution")
	private String temporalResolution;

	@JsonAlias("geospatial_resolution")
	private String geospatialResolution;

	private String annotations;

	private String maintainer;

	@JsonAlias("simulation_run")
	private Boolean simulationRun;
}
