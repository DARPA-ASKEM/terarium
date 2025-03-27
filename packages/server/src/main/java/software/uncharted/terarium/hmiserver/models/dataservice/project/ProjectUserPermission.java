package software.uncharted.terarium.hmiserver.models.dataservice.project;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.models.User;

@NoArgsConstructor
@Data
@Accessors(chain = true)
@Entity
public class ProjectUserPermission implements Serializable {

	@Id
	@TSIgnore
	private UUID id = UUID.randomUUID();

	@ManyToOne
	private User user;

	@ManyToOne
	private Project project;

	@Enumerated(EnumType.STRING)
	private ProjectPermissionLevel permissionLevel = ProjectPermissionLevel.NONE;
}
