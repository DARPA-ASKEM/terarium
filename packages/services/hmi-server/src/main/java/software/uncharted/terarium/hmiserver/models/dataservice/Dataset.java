package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Dataset implements Serializable {

	private String id;

	private String name;

	private String url;

	private String description;

	private Instant timestamp;

	private Boolean deprecated;

	private String sensitivity;

	private String quality;

	@JsonbProperty("temporal_resolution")
	private String temporalResolution;

	@JsonbProperty("geospatial_resolution")
	private String geospatialResolution;

	private String annotations;

	private String maintainer;

	@JsonbProperty("simulation_run")
	private Boolean simulationRun;
}
