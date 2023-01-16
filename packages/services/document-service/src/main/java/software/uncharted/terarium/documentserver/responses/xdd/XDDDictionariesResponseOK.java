package software.uncharted.terarium.documentserver.responses.xdd;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.documentserver.models.xdd.Dictionary;

import java.util.List;


@Data
@Accessors(chain = true)
public class XDDDictionariesResponseOK extends XDDResponseOK {

	private List<Dictionary> data;
}
