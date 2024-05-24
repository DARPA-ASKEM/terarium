package software.uncharted.terarium.hmiserver.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.springframework.data.util.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import lombok.Data;

public class AssetDependencyUtil {

	@Data
	static public class AssetDependencyMap {

		void addKeyId(final List<String> path, final UUID id) {
			keyIds.add(Pair.of(id, path));
			ids.add(id);
		}

		void addValueId(final List<String> path, final UUID id) {
			valueIds.add(Pair.of(id, path));
			ids.add(id);
		}

		List<Pair<UUID, List<String>>> keyIds = new ArrayList<>();
		List<Pair<UUID, List<String>>> valueIds = new ArrayList<>();
		Set<UUID> ids = new HashSet<>();
	}

	public static <T> AssetDependencyMap getAssetDependencies(final Set<UUID> assetIds, final T asset) {

		final ObjectMapper mapper = new ObjectMapper();

		final JsonNode assetJson = mapper.valueToTree(asset);

		final AssetDependencyMap dependencies = new AssetDependencyMap();

		executeOnEveryKeyAndText(new ArrayList<>(), assetJson,
				(final List<String> path, final String key) -> {
					final UUID uuid = uuidOrNull(key);
					if (uuid != null && assetIds.contains(uuid)) {
						dependencies.addKeyId(path, uuid);
					}
				},
				(final List<String> path, final String value) -> {
					final UUID uuid = uuidOrNull(value);
					if (uuid != null && assetIds.contains(uuid)) {
						dependencies.addValueId(path, uuid);
					}
				});

		return dependencies;
	}

	private static JsonNode traversePathAndGetNode(JsonNode node, final List<String> path) {
		for (final String key : path) {
			if (node.isArray()) {
				final int index = Integer.parseInt(key);
				node = node.get(index);
			} else if (node.isObject()) {
				node = node.get(key);
			} else {
				throw new RuntimeException("Path element is not an object or array: " + key);
			}
		}
		return node;
	}

	private static Pair<JsonNode, String> traversePathAndGetParent(JsonNode node, final List<String> path) {
		for (int i = 0; i < path.size() - 1; i++) {
			final String key = path.get(i);
			if (node.isArray()) {
				final int index = Integer.parseInt(key);
				node = node.get(index);
			} else if (node.isObject()) {
				node = node.get(key);
			} else {
				throw new RuntimeException("Path element is not an object or array: " + key);
			}
		}
		return Pair.of(node, path.get(path.size() - 1));
	}

	public static JsonNode swapAssetDependencies(
			final JsonNode asset,
			final Map<UUID, UUID> assetIdMapping,
			final AssetDependencyMap dependencies) {

		final ObjectNode res = (ObjectNode) asset.deepCopy();

		// replace key ids
		for (final Pair<UUID, List<String>> keyId : dependencies.getKeyIds()) {

			final UUID id = keyId.getFirst();
			if (!assetIdMapping.containsKey(id)) {
				throw new RuntimeException("No id mapping for id: " + keyId.getSecond());
			}
			final UUID newId = assetIdMapping.get(id);

			final List<String> path = keyId.getSecond();
			final JsonNode node = traversePathAndGetNode(res, path);

			// do the final swap
			final JsonNode value = node.get(id.toString());
			((ObjectNode) node).remove(id.toString());
			((ObjectNode) node).set(newId.toString(), value);
		}

		// replace value ids
		for (final Pair<UUID, List<String>> valueId : dependencies.getValueIds()) {
			final UUID id = valueId.getFirst();
			if (!assetIdMapping.containsKey(id)) {
				throw new RuntimeException("No id mapping for id: " + valueId.getSecond());
			}
			final UUID newId = assetIdMapping.get(id);

			final List<String> path = valueId.getSecond();
			final Pair<JsonNode, String> parentAndKey = traversePathAndGetParent(res, path);

			final JsonNode parent = parentAndKey.getFirst();

			final String key = parentAndKey.getSecond();

			// replace the text value
			if (parent.isArray()) {
				final int index = Integer.parseInt(key);
				((ArrayNode) parent).set(index, new TextNode(newId.toString()));
			} else {
				((ObjectNode) parent).set(key, new TextNode(newId.toString()));
			}
		}

		return res;
	}

	private static UUID uuidOrNull(final String str) {
		try {
			final UUID uuid = UUID.fromString(str);
			return uuid;
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	private static void executeOnEveryKeyAndText(
			final List<String> path,
			final JsonNode node,
			final BiConsumer<List<String>, String> keyFunc,
			final BiConsumer<List<String>, String> valueFunc) {

		if (node.isObject()) {
			final Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
			while (iterator.hasNext()) {
				final Map.Entry<String, JsonNode> entry = iterator.next();
				final String key = entry.getKey();
				final JsonNode value = entry.getValue();

				keyFunc.accept(path, key);

				final List<String> pathCopy = new ArrayList<>(path);
				pathCopy.add(key);

				executeOnEveryKeyAndText(pathCopy, value, keyFunc, valueFunc);
			}
		} else if (node.isArray()) {
			int i = 0;
			for (final JsonNode subNode : node) {

				final List<String> pathCopy = new ArrayList<>(path);
				pathCopy.add(String.valueOf(i));

				executeOnEveryKeyAndText(pathCopy, subNode, keyFunc, valueFunc);
				i++;
			}
		} else if (node.isTextual()) {
			valueFunc.accept(path, node.asText());
		}

	}
}
