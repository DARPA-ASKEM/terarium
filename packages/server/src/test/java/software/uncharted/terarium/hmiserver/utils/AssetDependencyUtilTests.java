package software.uncharted.terarium.hmiserver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.uncharted.terarium.hmiserver.utils.AssetDependencyUtil.AssetDependencyMap;

public class AssetDependencyUtilTests {

	@Autowired
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testAssetGetAssetDependencies() throws Exception {
		final UUID a = UUID.randomUUID();
		final UUID b = UUID.randomUUID();
		final UUID c = UUID.randomUUID();

		final Set<UUID> assetIds = new HashSet<>();
		assetIds.add(a);
		assetIds.add(b);
		assetIds.add(c);

		final JsonNode node = objectMapper.readTree(
			"""
				{
					"values": {
						"a": "%s",
						"b": "%s",
						"c": "%s"
					},
					"keys": {
						"%s": "a",
						"%s": "b",
						"%s": "c"
					},
					"array": [
						"%s",
						"%s",
						"%s"
					]
			}""".formatted(a, b, c, a, b, c, a, b, c)
		);

		final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(assetIds, node);

		// assert that the asset id is in the dependencies

		Assertions.assertEquals(3, dependencies.getIds().size());
		Assertions.assertTrue(dependencies.getIds().contains(a));
		Assertions.assertTrue(dependencies.getIds().contains(b));
		Assertions.assertTrue(dependencies.getIds().contains(c));

		Assertions.assertEquals(3, dependencies.getKeyIds().size());
		Assertions.assertEquals(6, dependencies.getValueIds().size());
	}

	@Test
	public void testAssetSwapAssetIdsSimple() throws Exception {
		final String format =
			"""
				{
					"values": {
						"a": "%s",
						"b": "%s",
						"c": "%s"
					},
					"keys": {
						"%s": "a",
						"%s": "b",
						"%s": "c"
					},
					"array": [
						"%s",
						"%s",
						"%s"
					]
			}""";

		final UUID a = UUID.randomUUID();
		final UUID b = UUID.randomUUID();
		final UUID c = UUID.randomUUID();

		final Set<UUID> assetIds = Set.of(a, b, c);

		final JsonNode node = objectMapper.readTree(format.formatted(a, b, c, a, b, c, a, b, c));

		final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(assetIds, node);

		// assert that the asset id is in the dependencies

		Assertions.assertEquals(3, dependencies.getIds().size());
		Assertions.assertTrue(dependencies.getIds().contains(a));
		Assertions.assertTrue(dependencies.getIds().contains(b));
		Assertions.assertTrue(dependencies.getIds().contains(c));

		Assertions.assertEquals(3, dependencies.getKeyIds().size());
		Assertions.assertEquals(6, dependencies.getValueIds().size());

		final UUID d = UUID.randomUUID();
		final UUID e = UUID.randomUUID();
		final UUID f = UUID.randomUUID();

		final Set<UUID> newAssetIds = Set.of(d, e, f);
		final Map<UUID, UUID> assetIdMap = Map.of(a, d, b, e, c, f);

		final JsonNode swapped = AssetDependencyUtil.swapAssetDependencies(node, assetIdMap, dependencies);

		final JsonNode newNode = objectMapper.readTree(format.formatted(d, e, f, d, e, f, d, e, f));

		Assertions.assertEquals(newNode, swapped);

		final AssetDependencyMap swappedDependencies = AssetDependencyUtil.getAssetDependencies(newAssetIds, swapped);

		Assertions.assertEquals(3, swappedDependencies.getIds().size());
		Assertions.assertTrue(swappedDependencies.getIds().contains(d));
		Assertions.assertTrue(swappedDependencies.getIds().contains(e));
		Assertions.assertTrue(swappedDependencies.getIds().contains(f));

		Assertions.assertEquals(3, swappedDependencies.getKeyIds().size());
		Assertions.assertEquals(6, swappedDependencies.getValueIds().size());
	}

	@Test
	public void testAssetSwapAssetIdsComplicated() throws Exception {
		final String format =
			"""
				{
					"values": {
						"nested": [
							{
								"deep": "%s"
							},
							"%s"
						],
						"some": {
							"other": {
								"value": "%s"
							}
						}
					},
					"keys": {
						"nested": {
							"array": [
								{
									"%s": "a"
								},
								{
									"%s": "b"
								}
							]
						},
						"%s": "c"
					},
					"array": [
						[
							"%s",
							[
								"%s"
							]
						],
						[
							[ 0, 2, ["%s"]]
						]
					]
			}""";

		final UUID a = UUID.randomUUID();
		final UUID b = UUID.randomUUID();
		final UUID c = UUID.randomUUID();

		final Set<UUID> assetIds = Set.of(a, b, c);

		final JsonNode node = objectMapper.readTree(format.formatted(a, b, c, a, b, c, a, b, c));

		final AssetDependencyMap dependencies = AssetDependencyUtil.getAssetDependencies(assetIds, node);

		// assert that the asset id is in the dependencies

		Assertions.assertEquals(3, dependencies.getIds().size());
		Assertions.assertTrue(dependencies.getIds().contains(a));
		Assertions.assertTrue(dependencies.getIds().contains(b));
		Assertions.assertTrue(dependencies.getIds().contains(c));

		Assertions.assertEquals(3, dependencies.getKeyIds().size());
		Assertions.assertEquals(6, dependencies.getValueIds().size());

		final UUID d = UUID.randomUUID();
		final UUID e = UUID.randomUUID();
		final UUID f = UUID.randomUUID();

		final Set<UUID> newAssetIds = Set.of(d, e, f);
		final Map<UUID, UUID> assetIdMap = Map.of(a, d, b, e, c, f);

		final JsonNode swapped = AssetDependencyUtil.swapAssetDependencies(node, assetIdMap, dependencies);

		final JsonNode newNode = objectMapper.readTree(format.formatted(d, e, f, d, e, f, d, e, f));

		Assertions.assertEquals(newNode, swapped);

		final AssetDependencyMap swappedDependencies = AssetDependencyUtil.getAssetDependencies(newAssetIds, swapped);

		Assertions.assertEquals(3, swappedDependencies.getIds().size());
		Assertions.assertTrue(swappedDependencies.getIds().contains(d));
		Assertions.assertTrue(swappedDependencies.getIds().contains(e));
		Assertions.assertTrue(swappedDependencies.getIds().contains(f));

		Assertions.assertEquals(3, swappedDependencies.getKeyIds().size());
		Assertions.assertEquals(6, swappedDependencies.getValueIds().size());
	}
}
