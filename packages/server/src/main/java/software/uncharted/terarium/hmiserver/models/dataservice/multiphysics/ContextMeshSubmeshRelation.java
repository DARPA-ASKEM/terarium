package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class ContextMeshSubmeshRelation implements Serializable {
	private String mesh;
	private String submesh;
	private Object relation;
}
