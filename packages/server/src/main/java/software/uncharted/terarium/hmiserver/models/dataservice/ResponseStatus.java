package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@Data
@TSModel
@Accessors(chain = true)
public class ResponseStatus {

  public ResponseStatus(Integer status) {
    this.status = status;
  }

  private Integer status;
}
