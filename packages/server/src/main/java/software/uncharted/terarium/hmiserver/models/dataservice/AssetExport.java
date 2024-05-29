package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@Data
@Accessors(chain = true)
public class AssetExport {
	AssetType type;
	TerariumAsset asset;
	Map<String, FileExport> files = new HashMap<>();
}
