package software.uncharted.terarium.hmiserver.models.modelservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Edge implements Serializable{
	private String source;
	private String target;
}
