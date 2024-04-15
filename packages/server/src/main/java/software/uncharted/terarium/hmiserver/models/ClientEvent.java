package software.uncharted.terarium.hmiserver.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;


import java.io.Serializable;
import java.util.UUID;

@Builder
@Value
@TSModel
public class ClientEvent<T> implements Serializable {
  @Builder.Default private String id = UUID.randomUUID().toString();
  @Builder.Default private long createdAtMs = System.currentTimeMillis();
  private ClientEventType type;
  private T data;
}
