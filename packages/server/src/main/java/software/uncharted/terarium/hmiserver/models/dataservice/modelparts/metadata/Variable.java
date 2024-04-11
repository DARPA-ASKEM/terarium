package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class Variable extends SupportAdditionalProperties implements Serializable {
  @Serial private static final long serialVersionUID = 701749887670320136L;

  private String id;

  private String name;

  private List<VariableMetadata> metadata;

  @JsonProperty("dkg_groundings")
  private List<DKGConcept> dkgGroundings;

  private List<DataColumn> column;

  private Paper paper;

  private List<EquationVariable> equations;
}
