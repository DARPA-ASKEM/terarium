package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DocumentExtraction implements Serializable {

	public static final String TABLE_ASSETTYPE = "table";
	public static final String EQUATION_ASSETTYPE = "equation";
	public static final String FIGURE_ASSETTYPE = "figure";



	@JsonAlias("file_name")
	private String fileName;

	@JsonAlias("asset_type")
	private String assetType;

	private Map<String,Object> metadata;
}
