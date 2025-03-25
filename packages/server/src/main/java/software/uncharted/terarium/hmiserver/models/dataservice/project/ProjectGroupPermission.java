package software.uncharted.terarium.hmiserver.models.dataservice.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.models.Group;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@NoArgsConstructor
@Data
@Accessors(chain = true)
@Entity
public class ProjectGroupPermission implements Serializable {

	@Id
	@TSIgnore
	private UUID id = UUID.randomUUID();

	@ManyToOne
	private Group group;

	@ManyToOne
	private Project project;

	@Enumerated(EnumType.STRING)
	private ProjectPermissionLevel permissionLevel = ProjectPermissionLevel.READ;
}
