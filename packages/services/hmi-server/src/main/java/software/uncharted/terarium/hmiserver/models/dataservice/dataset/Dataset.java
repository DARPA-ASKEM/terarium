package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TSModel
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

	private DatasetAnnotations annotations;

	private String maintainer;

	@JsonAlias("simulation_run")
	private Boolean simulationRun;
}
