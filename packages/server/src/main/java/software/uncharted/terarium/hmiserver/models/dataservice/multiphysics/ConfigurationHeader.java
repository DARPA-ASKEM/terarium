package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@Accessors(chain = true)
@TSModel
public class ConfigurationHeader implements Serializable {

  @Serial private static final long serialVersionUID = 1449501982399992143L;

  private String id;

  private String description;

  private String name;

  @JsonAlias("parent_context")
  private String parentContext;
}
