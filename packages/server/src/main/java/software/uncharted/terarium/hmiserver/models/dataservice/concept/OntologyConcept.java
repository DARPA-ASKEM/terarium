package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.dataservice.TerariumAsset;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Accessors(chain = true)
@TSModel
public class OntologyConcept extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 5877193452400957711L;


	private String curie;

	@ManyToOne
	@JoinColumn(name = "activeConceptId", nullable = true)
	@JsonBackReference
	private ActiveConcept activeConcept;

	private TaggableType type;

	@JsonAlias("object_id")
	private UUID objectId;

	private OntologicalField status;

}
