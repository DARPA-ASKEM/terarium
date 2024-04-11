package software.uncharted.terarium.hmiserver.models.dataservice.modelparts;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class ModelGrounding extends SupportAdditionalProperties implements Serializable {
  @Serial private static final long serialVersionUID = -4946214209697566543L;

  private Map<String, Object> identifiers;

  @TSOptional private Map<String, Object> context;

  @TSOptional private Object modifiers;
}
