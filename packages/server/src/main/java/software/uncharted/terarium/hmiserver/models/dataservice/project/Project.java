package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

import java.io.Serial;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Project extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -241733670076432802L;

	@Schema(defaultValue = "My New Project")
	private String name;

	private String userId;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String userName;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private List<String> authors;

	@TSOptional
	@Schema(defaultValue = "My Project Description")
	private String description;

	@TSOptional
	@Schema(defaultValue = "My Project Overview")
	@Lob
	@JdbcTypeCode(Types.BINARY)
	private byte[] overviewContent;

	@OneToMany(mappedBy = "project")
	@Where(clause = "deleted_on IS NULL")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@ToString.Exclude
	@JsonManagedReference
	private List<ProjectAsset> projectAssets = new ArrayList<>();

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	private Map<String, String> metadata;

	/** Information for the front-end to display/filter the project accordingly. */
	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Boolean publicProject;

	/** Information for the front-end to enable/disable features based on user permissions (Read/Write). */
	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String userPermission;

	public static Project mergeProjectFields(final Project existingProject, final Project project) {
		// Merge non-transient Project specific fields into the existing project
		if (project.getName() != null) {
			existingProject.setName(project.getName());
		}
		if (project.getDescription() != null) {
			existingProject.setDescription(project.getDescription());
		}
		if (project.getOverviewContent() != null) {
			existingProject.setOverviewContent(project.getOverviewContent());
		}
		return existingProject;
	}
}
