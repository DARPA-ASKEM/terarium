package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;

@Entity
@Data
@Accessors(chain = true)
public class OntologyConcept implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	private String curie;

	@ManyToOne
	@JoinColumn(name = "activeConceptId", nullable = true)
	@JsonBackReference
	private ActiveConcept activeConcept;

	private TaggableType type;

	@JsonAlias("object_id")
	private UUID objectId;

	private OntologicalField status;

	@CreationTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp createdOn;

	@UpdateTimestamp
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp updatedOn;

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;
}
