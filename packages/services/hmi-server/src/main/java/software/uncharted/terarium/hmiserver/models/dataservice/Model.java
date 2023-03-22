package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Accessors(chain = true)
public class Model implements Serializable {

	private Long id;

	private String name;

	private String description;

	private String framework;

	private LocalDateTime timestamp;

	private ModelContent content;

	private Concept concept;

	private List<Object> parameters;
}

