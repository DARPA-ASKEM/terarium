package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.multiphysics.DecapodesConfiguration;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class DecapodesConfigurationService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	@Observed(name = "function_profile")
	public List<DecapodesConfiguration> getDecapodesConfigurations(final Integer page, final Integer pageSize)
		throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
			.index(elasticConfig.getDecapodesConfigurationIndex())
			.size(pageSize)
			.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
			.sort(
				new SortOptions.Builder().field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build()).build()
			)
			.build();

		return elasticService.search(req, DecapodesConfiguration.class);
	}

	@Observed(name = "function_profile")
	public Optional<DecapodesConfiguration> getDecapodesConfiguration(final UUID id) throws IOException {
		final DecapodesConfiguration doc = elasticService.get(
			elasticConfig.getDecapodesConfigurationIndex(),
			id.toString(),
			DecapodesConfiguration.class
		);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	@Observed(name = "function_profile")
	public void deleteDecapodesConfiguration(final UUID id) throws IOException {
		final Optional<DecapodesConfiguration> decapodesConfiguration = getDecapodesConfiguration(id);
		if (decapodesConfiguration.isEmpty()) {
			return;
		}
		decapodesConfiguration.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateDecapodesConfiguration(decapodesConfiguration.get());
	}

	@Observed(name = "function_profile")
	public DecapodesConfiguration createDecapodesConfiguration(final DecapodesConfiguration decapodesConfiguration)
		throws IOException {
		decapodesConfiguration.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(
			elasticConfig.getDecapodesConfigurationIndex(),
			decapodesConfiguration.setId(UUID.randomUUID()).getId().toString(),
			decapodesConfiguration
		);
		return decapodesConfiguration;
	}

	@Observed(name = "function_profile")
	public Optional<DecapodesConfiguration> updateDecapodesConfiguration(
		final DecapodesConfiguration decapodesConfiguration
	) throws IOException {
		if (
			!elasticService.documentExists(
				elasticConfig.getDecapodesConfigurationIndex(),
				decapodesConfiguration.getId().toString()
			)
		) {
			return Optional.empty();
		}
		decapodesConfiguration.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(
			elasticConfig.getDecapodesConfigurationIndex(),
			decapodesConfiguration.getId().toString(),
			decapodesConfiguration
		);
		return Optional.of(decapodesConfiguration);
	}
}
