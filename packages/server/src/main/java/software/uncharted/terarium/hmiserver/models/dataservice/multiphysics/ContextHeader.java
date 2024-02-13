package software.uncharted.terarium.hmiserver.models.dataservice.multiphysics;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class ContextHeader implements Serializable {

	@Serial
	private static final long serialVersionUID = -2060949623709593169L;

	private String id;

	private String description;

	private String name;

	@JsonAlias("parent_model")
	private String parentModel;
}
