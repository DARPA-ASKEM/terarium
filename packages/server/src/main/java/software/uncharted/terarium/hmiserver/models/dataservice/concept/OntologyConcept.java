package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;

@Entity
@Data
@Accessors(chain = true)
@TSModel
public class OntologyConcept implements Serializable {

  @Serial private static final long serialVersionUID = 5877193452400957711L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @TSOptional
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  private UUID id;

  @TSOptional
  @CreationTimestamp
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Timestamp createdOn;

  @TSOptional
  @UpdateTimestamp
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Timestamp updatedOn;

  @TSOptional
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Timestamp deletedOn;

  private String curie;

  @ManyToOne
  @JoinColumn(name = "activeConceptId", nullable = true)
  @JsonBackReference
  @JsonAlias("active_concept")
  private ActiveConcept activeConcept;

  private TaggableType type;

  @JsonAlias("object_id")
  private UUID objectId;

  private OntologicalField status;
}
