package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

@Data
@Accessors(chain = true)
@TSModel
public class DecapodesComponent {
	@JsonAlias("interface")
	private List<String> modelInterface;
	private DecapodesExpression model;
	private String _type;
}
