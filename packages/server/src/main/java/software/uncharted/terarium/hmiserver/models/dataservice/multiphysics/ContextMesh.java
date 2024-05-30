package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ContextMesh implements Serializable {
	private String id;
	private String description;
	private Object dimensionality;

	@JsonAlias("vertex_count")
	private Long vertexCount;

	@JsonAlias("edge_count")
	private Long edgeCount;

	@JsonAlias("face_count")
	private Long faceCount;

	@JsonAlias("volume_count")
	private Long volumeCount;

	private List<Object> regions;

	private String checksum;

	private ContextFile file;
}
