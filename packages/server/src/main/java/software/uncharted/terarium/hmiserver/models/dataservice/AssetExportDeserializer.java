package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

public class AssetExportDeserializer extends JsonDeserializer<AssetExport> {

	@Override
	public AssetExport deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		final JsonNode node = mapper.readTree(jp);

		final String assetTypeStr = node.get("type").asText();
		final AssetType assetType = AssetType.getAssetType(assetTypeStr, mapper);

		final TerariumAsset asset = mapper.treeToValue(node.get("asset"), assetType.getAssetClass());

		Map<String, FileExport> files = new HashMap<>();
		if (node.hasNonNull("files")) {
			files = mapper.convertValue(node.get("files"), new TypeReference<Map<String, FileExport>>() {});
		}

		final AssetExport export = new AssetExport();
		export.setType(assetType);
		export.setAsset(asset);
		export.setFiles(files);
		return export;
	}
}
