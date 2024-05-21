package software.uncharted.terarium.hmiserver.models.dataservice;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AssetExport<T> {

	T asset;

	Map<String, FileExport> files = new HashMap<>();
}
