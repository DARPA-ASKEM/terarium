package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.checkerframework.checker.units.qual.A;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;

/**
 * Represents a generic artifact that can be stored in the data service. For example, this could be a text file, a code
 * file, a zip file, or anything else. It should not be used for a dataset or a model, which have their own classes.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Artifact extends TerariumAsset {
	@Serial
	private static final long serialVersionUID = -1122602270904707476L;

	/* UserId of who created this asset */
	@Column(length = 255)
	private String userId;

	/* The name of the file(s) in this artifact */
	@JsonAlias("file_names")
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<String> fileNames;

	/* metadata for these files */
	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode metadata;

	@TSOptional
	@ManyToOne
	@JsonBackReference
	private Project project;

	@Override
	public Artifact clone(){
		final Artifact clone = new Artifact();
		cloneSuperFields(clone);

		clone.userId = this.userId;

		if (this.fileNames != null) {
			clone.fileNames = new ArrayList<>();
			clone.fileNames.addAll(this.fileNames);
		}

		if(this.metadata != null){
			clone.metadata = this.metadata.deepCopy();
		}

		return clone;
	}


}
