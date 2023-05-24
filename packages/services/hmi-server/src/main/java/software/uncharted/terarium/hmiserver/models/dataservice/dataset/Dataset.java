package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TSModel
public class Dataset implements Serializable {

	@TSOptional
	private Long id;

	private String name;

	private String url;

	private String description;

	@TSOptional
	private LocalDateTime timestamp;

	@TSOptional
	private Boolean deprecated;

	@TSOptional
	private String sensitivity;

	@TSOptional
	private String quality;

	@TSOptional
	@JsonAlias("temporal_resolution")
	private String temporalResolution;

	@TSOptional
	@JsonAlias("geospatial_resolution")
	private String geospatialResolution;

	@TSOptional
	private DatasetAnnotations annotations;

	@TSOptional
	private String maintainer;

	@TSOptional
	@JsonAlias("simulation_run")
	private Boolean simulationRun;
}
