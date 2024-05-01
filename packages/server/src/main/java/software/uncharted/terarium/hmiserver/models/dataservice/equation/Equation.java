package software.uncharted.terarium.hmiserver.models.dataservice.equation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.util.HashMap;import java.util.Map;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;import org.hibernate.type.SqlTypes;import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;

/** The Equation Data Model */
@EqualsAndHashCode(callSuper = true)
@TSModel
@Data
@Accessors(chain = true)
@Entity
public class Equation extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -5769056715284691520L;

	/** The userId of the user that created the equation * */
	@TSOptional
	@Column(length = 255)
	private String userId;

	/** The type of equation (mathml or latex) * */
	@JsonAlias("equation_type")
	private EquationType equationType;

	/** String representation of the equation * */
	@Column(columnDefinition = "text")
	private String content;

	/** (Optional) Unformatted metadata about the equation * */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, JsonNode> metadata;

	/** (Optional) Source of the equation, whether a document or HMI generated * */
	@TSOptional
	@JdbcTypeCode(SqlTypes.JSON)
	private EquationSource source;


	@Override
	public Equation clone(){
		final Equation clone = new Equation();
		cloneSuperFields(clone);

		clone.userId = this.userId;
		clone.equationType = this.equationType;
		clone.content = this.content;
		if(this.metadata != null){
			clone.metadata = new HashMap<>(this.metadata);
			for(final String key : this.metadata.keySet()) {
				clone.metadata.put(key, this.metadata.get(key).deepCopy());
			}
		}
		clone.source = this.source.clone();


		return clone;
	}
}
