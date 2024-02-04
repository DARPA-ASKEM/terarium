package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import java.io.Serializable;
import java.util.Map;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class Context implements Serializable {

	private Object constants;

	@JsonAlias("spatial_constraints")
	private Object spatialConstraints;

	@JsonAlias("temporal_constraints")
	private Object temporalConstraints;

	@JsonAlias("primal_dual_relations")
	private List<Object> primalDualRelations;

	@JsonAlias("mesh_submesh_relations")
	private List<Object> meshSubmeshRelations;

	private List<Object> meshes;
}
