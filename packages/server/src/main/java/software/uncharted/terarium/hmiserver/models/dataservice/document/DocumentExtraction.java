package software.uncharted.terarium.hmiserver.models.dataservice.document;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

	public static final String TABLE_ASSETTYPE = "Table";
	public static final String EQUATION_ASSETTYPE = "Equation";
	public static final String FIGURE_ASSETTYPE = "Figure";



	@JsonAlias("file_name")
	private String fileName;

	@JsonAlias("asset_type")
	private ExtractionAssetType assetType;

	private Map<String,Object> metadata;

	public enum ExtractionAssetType {
		@JsonAlias({"figure", "Figure"})
		FIGURE,
		@JsonAlias({"table", "Table"})
		TABLE,
		@JsonAlias({"equation", "Equation"})
		EQUATION;	

		public static ExtractionAssetType fromString(String type) {
			for (ExtractionAssetType assetType : ExtractionAssetType.values()) {
				if (assetType.toString().equalsIgnoreCase(type)) {
					return assetType;
				}
			}
			throw new IllegalArgumentException("No constant with type " + type + " found");
		}
	}
}
