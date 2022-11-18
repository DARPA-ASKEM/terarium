package software.uncharted.terarium.hmiserver.models.dataservice;

import javax.json.bind.annotation.JsonbProperty;
import java.time.Instant;

public class Dataset {
	@JsonbProperty("id")
	public String id;

	@JsonbProperty("name")
	public String name;

	@JsonbProperty("url")
	public String url;

	@JsonbProperty("description")
	public String description;

	@JsonbProperty("timestamp")
	public Instant timestamp;

	@JsonbProperty("deprecated")
	public Boolean deprecated;

	@JsonbProperty("sensitivity")
	public String sensitivity;

	@JsonbProperty("quality")
	public String quality;

	@JsonbProperty("temporal_resolution")
	public String temporalResolution;

	@JsonbProperty("geospatial_resolution")
	public String geospatialResolution;

	@JsonbProperty("annotations")
	public String annotations;

	@JsonbProperty("maintainer")
	public String maintainer;
}
