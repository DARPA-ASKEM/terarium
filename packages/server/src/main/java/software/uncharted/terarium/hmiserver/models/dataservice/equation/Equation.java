package software.uncharted.terarium.hmiserver.models.dataservice.equation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

/**
 * The Equation Data Model
 */
@TSModel
@Data
@Accessors(chain = true)
public class Equation {

	/** Universally unique identifier for the item **/
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

	/** The userId of the user that created the equation **/
	@TSOptional
	private String userId;

	/** (Optional) Display/human name for the equation **/
	@TSOptional
	private String name;

	/** The type of equation (mathml or latex) **/
	@JsonAlias("equation_type")
	private EquationType equationType;

	/** String representation of the equation **/
	private String content;

	/** (Optional) Unformatted metadata about the equation **/
	@TSOptional
	private Map<String, JsonNode> metadata;

	/** (Optional) Source of the equation, whether a document or HMI generated **/
	@TSOptional
	private EquationSource source;

}
