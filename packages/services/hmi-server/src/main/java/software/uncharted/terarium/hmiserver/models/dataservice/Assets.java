package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.documentservice.Extraction;

import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@Slf4j
public class Assets implements Serializable {
	List<Dataset> datasets;
	List<Extraction> extractions;
	List<Model> models;
	List<DocumentAsset> publications;
	List<Workflow> workflows;
	List<Artifact> artifacts;


	public enum AssetType {
		DATASETS("datasets"),
		MODEL_CONFIGURATIONS("model_configurations"),
		MODELS("models"),
		PUBLICATIONS("publications"),
		SIMULATIONS("simulations"),
		WORKFLOWS("workflows"),
		ARTIFACTS("artifacts");
		//CODE("code");




		public final String type;


		AssetType(final String type) {
			this.type = type;
		}

		@Override
		@JsonValue
		public String toString() {
			return type;
		}

		public static AssetType fromString(final String type) {
			return Arrays.stream(AssetType.values())
					.filter(t -> t.type.equals(type))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unknown resource type: " + type));
		}
	}
}
