package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Accessors(chain = true)
@TSModel
public class ActiveConcept extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 3747098342861343228L;

	@Column(unique = true)
	private String curie;
}
