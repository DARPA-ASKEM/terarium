package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.type.SqlTypes;import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

/** Represents a grounding document from TDS */
@Data
@Accessors(chain = true)
@TSModel
public class Grounding implements Serializable {

	@Serial
	private static final long serialVersionUID = 302308407252037615L;

	/** Ontological identifier per DKG */
	@JdbcTypeCode(SqlTypes.JSON)
	private List<Identifier> identifiers;

	/** (Optional) Additional context that informs the grounding */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, Object> context;
}
