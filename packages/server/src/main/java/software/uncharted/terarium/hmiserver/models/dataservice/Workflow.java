package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * The workflow data structure is not very well defined. It is also meant to carry operations each with their own unique
 * representations. As such this is just a pass-thru class for the proxy. The UI has it's own typinging definition that is
 * not generated.
 */
@Data
@Accessors(chain = true)
public class Workflow implements Serializable {

	private String id;

	private String name;

	private String description;

	private Object transform;

	private List<Object> nodes;

	private List<Object> edges;
}
