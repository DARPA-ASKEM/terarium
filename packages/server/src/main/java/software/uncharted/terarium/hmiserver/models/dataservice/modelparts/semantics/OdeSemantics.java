package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;

@Data
@AMRSchemaType
@Accessors(chain = true)
public class OdeSemantics extends SupportAdditionalProperties implements Serializable {
  @Serial private static final long serialVersionUID = 8943488983879443909L;

  private List<Rate> rates;

  @TSOptional private List<Initial> initials;

  @TSOptional private List<ModelParameter> parameters;

  @TSOptional private List<Observable> observables;

  @TSOptional private JsonNode time;
}
