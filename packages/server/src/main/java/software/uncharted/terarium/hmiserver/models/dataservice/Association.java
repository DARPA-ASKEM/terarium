package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Association implements Serializable {

	@Serial
	private static final long serialVersionUID = 7927618258919445963L;

	private String id;

	@JsonProperty("person_id")
	private String personId;

	@JsonProperty("asset_id")
	private String assetId;

	@JsonProperty("asset_type")
	private AssetType assetType;

	private Role role;
}
