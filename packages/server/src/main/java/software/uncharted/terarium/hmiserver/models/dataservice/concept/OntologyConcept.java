package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@Accessors(chain = true)
@TSModel
public class OntologyConcept implements Serializable {

	@Serial
	private static final long serialVersionUID = 5877193452400957711L;

	@Id
	private String curie;

	@ManyToOne
	@JoinColumn(name = "activeConceptId", nullable = true)
	@JsonBackReference
	@JsonAlias("active_concept")
	private ActiveConcept activeConcept;

	private TaggableType type;

	@JsonAlias("object_id")
	private String objectId;

	private OntologicalField status;

}
