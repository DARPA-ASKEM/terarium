package software.uncharted.terarium.hmiserver.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;


import java.io.Serializable;
import java.util.UUID;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@TSModel
public class ClientEvent<T> implements Serializable {
  private String id = UUID.randomUUID().toString();
  private long createdAtMs = System.currentTimeMillis();
  private ClientEventType type;
  private T data;
}
