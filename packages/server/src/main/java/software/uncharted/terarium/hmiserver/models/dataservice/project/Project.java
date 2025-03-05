package software.uncharted.terarium.hmiserver.models.dataservice.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.TerariumAssetEmbeddingType;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
@Slf4j
public class Project extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -241733670076432802L;

	private String userId;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "default")
	private String thumbnail;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String userName;

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private List<String> authors;

	@TSOptional
	@Schema(defaultValue = "My Project Overview")
	@Lob
	@JdbcTypeCode(Types.BINARY)
	private byte[] overviewContent;

	@OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Where(clause = "deleted_on IS NULL")
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<ProjectAsset> projectAssets = new ArrayList<>();

	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	private Map<String, String> metadata;

	@TSOptional
	private Boolean sampleProject = false;

	/** Information for the front-end to display/filter the project accordingly. */
	@TSOptional
	@Transient
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Boolean publicProject;

	/**
	 * Information for the front-end to enable/disable features based on user
	 * permissions (Read/Write).
	 */
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
		if (project.getThumbnail() != null) {
			existingProject.setThumbnail(project.getThumbnail());
		}
		if (project.getSampleProject() != null) {
			existingProject.setSampleProject(project.getSampleProject());
		}
		return existingProject;
	}

	@Override
	public Project clone() {
		final Project cloned = new Project();
		cloneSuperFields(cloned);
		cloned.userId = userId;
		cloned.userName = userName;
		if (authors != null) {
			cloned.authors = new ArrayList<>(authors);
		}
		cloned.overviewContent = overviewContent;
		if (metadata != null) {
			cloned.metadata = new HashMap<>(metadata);
		}
		cloned.publicProject = publicProject;
		cloned.userPermission = userPermission;
		return cloned;
	}

	@JsonIgnore
	@TSIgnore
	public String getEmbeddingSourceText() {
		try {
			if (overviewContent != null) {
				return getOverviewAsReadableString();
			}
			final ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(this);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to serialize project embedding text into JSON", e);
		}
	}

	@JsonIgnore
	@TSIgnore
	public String getOverviewAsReadableString() {
		if (overviewContent == null) {
			return null;
		}

		// remove image tags
		final String regex = "<img\\b[^>]*>(.*?)<\\/img>|<img\\b[^>]*\\/>";
		final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(new String(overviewContent));
		return matcher.replaceAll("");
	}

	@JsonIgnore
	@TSIgnore
	public Map<TerariumAssetEmbeddingType, String> getEmbeddingsSourceByType() {
		final Map<TerariumAssetEmbeddingType, String> sources = super.getEmbeddingsSourceByType();

		if (overviewContent != null) {
			sources.put(TerariumAssetEmbeddingType.OVERVIEW, getOverviewAsReadableString());
		}

		return sources;
	}
}
