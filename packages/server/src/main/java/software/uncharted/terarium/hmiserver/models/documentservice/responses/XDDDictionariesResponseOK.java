package software.uncharted.terarium.hmiserver.models.documentservice.responses;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.documentservice.Dictionary;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class XDDDictionariesResponseOK extends XDDResponseOK {

  private List<Dictionary> data;
}
