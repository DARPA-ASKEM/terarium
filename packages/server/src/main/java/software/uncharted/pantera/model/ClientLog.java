package software.uncharted.pantera.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.pantera.annotations.TSModel;
import software.uncharted.pantera.annotations.TSOptional;

@TSModel
@Accessors(chain = true)
@NoArgsConstructor
@Data
public class ClientLog {
  private String level;
  private long timestampMillis;
  private String message;
  @TSOptional
  private String[] args;
}
