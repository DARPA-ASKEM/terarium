package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@Data
@Accessors(chain = true)
@JsonDeserialize(using = AssetExportDeserializer.class)
public class AssetExport {

	AssetType type;
	TerariumAsset asset;
	Map<String, FileExport> files = new HashMap<>();
}
