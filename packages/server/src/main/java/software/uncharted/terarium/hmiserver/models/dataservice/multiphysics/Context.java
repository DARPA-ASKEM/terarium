package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class Context implements Serializable {

  private Map<String, ContextConstant> constants;

  @JsonAlias("spatial_constraints")
  private JsonNode spatialConstraints;

  @JsonAlias("temporal_constraints")
  private JsonNode temporalConstraints;

  @JsonAlias("primal_dual_relations")
  private List<ContextPrimalDualRelation> primalDualRelations;

  @JsonAlias("mesh_submesh_relations")
  private List<ContextMeshSubmeshRelation> meshSubmeshRelations;

  private List<ContextMesh> meshes;
}
