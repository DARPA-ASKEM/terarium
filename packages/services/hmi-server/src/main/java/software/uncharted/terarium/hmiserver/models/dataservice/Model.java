package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Accessors(chain = true)
public class Model implements Serializable {

	private Long id;

	private String name;

	@JsonSetter(nulls = Nulls.SKIP)
	private String description = "";

	private String framework;

	private LocalDateTime timestamp;

	private ModelContent content;

	private Concept concept;

	private List<Object> parameters;
}
