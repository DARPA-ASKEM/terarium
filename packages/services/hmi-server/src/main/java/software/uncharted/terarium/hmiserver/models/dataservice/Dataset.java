package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Dataset implements Serializable {

	private Long id;

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

	//TODO not an object?
	private Object annotations;

	private String maintainer;

	@JsonAlias("simulation_run")
	private Boolean simulationRun;
}
