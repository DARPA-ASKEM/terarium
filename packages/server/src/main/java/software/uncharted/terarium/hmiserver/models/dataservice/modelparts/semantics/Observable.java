package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class Observable extends SupportAdditionalProperties implements Serializable {
  @Serial private static final long serialVersionUID = -8367278228176339223L;

  private String id;

  @TSOptional private String name;

  @TSOptional private List<String> states;

  @TSOptional private String expression;

  @TSOptional
  @JsonProperty("expression_mathml")
  private String expressionMathml;
}
