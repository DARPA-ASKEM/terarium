package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.Metamodel;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.DataMigration;
import software.uncharted.terarium.hmiserver.models.DataMigration.MigrationState;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.repository.DataMigrationRepository;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

/**
 * When migated asset services to use postgres as the central storage, this will migrate existing data from
 * elasticsearch into the postgres table, storing the result of the migration in pg so it doesn't do it multiple times.
 */
@Service
@Profile("!test") // don't run in test profile
@RequiredArgsConstructor
@Slf4j
public class DataMigrationESToPG {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;

	private final DataMigrationRepository migrationRepository;

	private final WorkflowService workflowService;
	private final SimulationService simulationService;
	private final CodeService codeService;

	@PersistenceContext
	private EntityManager entityManager;

	private String getTableName(final Class<?> entityClass) {
		final Metamodel metamodel = entityManager.getMetamodel();
		return metamodel.entity(entityClass).getName();
	}

	@Data
	private static class MigrationConfig<T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>> {

		final TerariumAssetServiceWithoutSearch<T, R> service;
		final String index;

		MigrationConfig(final TerariumAssetServiceWithoutSearch<T, R> service, final String index) {
			this.service = service;
			this.index = index;
		}

		void migrateFromEsToPg(final ElasticsearchService elasticService) throws IOException {
			// check if there is a target index to migrate from
			if (!elasticService.indexExists(index)) {
				return;
			}

			if (elasticService.count(index) == 0) {
				return;
			}

			// check if the data has already been migrated
			final int PAGE_SIZE = 256;
			Long lastId = null;
			Long lastPagesLastId = null;

			log.info("Migrating from ES index {} to SQL...", index);

			final String SORT_FIELD = "createdOn";

			while (true) {
				final SearchRequest.Builder reqBuilder = new SearchRequest.Builder()
						.index(index)
						.size(PAGE_SIZE)
						.sort(new SortOptions.Builder()
								.field(new FieldSort.Builder()
										.field(SORT_FIELD)
										.order(SortOrder.Asc)
										.build())
								.build());

				if (lastId != null) {
					reqBuilder.searchAfter(FieldValue.of(lastId));
				}

				final SearchRequest req = reqBuilder.build();

				final SearchResponse<T> resp = elasticService.searchWithResponse(req, service.getAssetClass());

				final List<T> assets = new ArrayList<>();
				for (final Hit<T> hit : resp.hits().hits()) {
					lastId = hit.sort().get(0).longValue();

					final T asset = hit.source();
					if (asset == null) {
						log.warn("Null document payload for id: {}, skipping", hit.id());
						continue;
					}
					if (asset.getId() == null || !asset.getId().toString().equals(hit.id())) {
						asset.setId(UUID.fromString(hit.id()));
					}
					assets.add(asset);
				}

				if (!assets.isEmpty()) {
					log.info("Saving {} rows to SQL...", assets.size());
					service.getRepository().saveAll(assets);
				}

				if (Objects.equals(lastId, lastPagesLastId) || assets.size() < PAGE_SIZE) {
					break;
				}

				// track the last id of the page to ensure we aren't just pulling the same page
				// over and over
				lastPagesLastId = lastId;
			}
		}
	}

	List<MigrationConfig<?, ?>> getMigrations() {
		return List.of(
				new MigrationConfig<>(workflowService, elasticConfig.getWorkflowIndex()),
				new MigrationConfig<>(simulationService, elasticConfig.getSimulationIndex()),
				new MigrationConfig<>(codeService, elasticConfig.getCodeIndex()));
		// TODO: Write a script to properly sync the old ProjectAsset to the new PG data
	}

	@PostConstruct
	void runMigrations() {
		for (final MigrationConfig<?, ?> migration : getMigrations()) {

			final String tableName = getTableName(migration.getService().getAssetClass());

			try {
				DataMigration migrationRecord =
						migrationRepository.findByTableName(tableName).orElse(null);

				if (migrationRecord != null && migrationRecord.getState() == MigrationState.SUCCESS) {
					log.info("Data already migrated for table: {}", tableName);
					continue;
				}

				migration.migrateFromEsToPg(elasticService);

				migrationRecord = new DataMigration();
				migrationRecord.setTableName(tableName);
				migrationRecord.setTimestamp(new Timestamp(System.currentTimeMillis()));
				migrationRecord.setState(MigrationState.SUCCESS);
				migrationRepository.save(migrationRecord);

				log.info("Migrated ES index {} to PG table {} successfully", migration.getIndex(), tableName);

			} catch (final Exception e) {
				log.warn("Failed to migrate data from ES index {} to PG table {}", migration.getIndex(), tableName, e);

				final DataMigration migrationRecord = new DataMigration();
				migrationRecord.setTableName(tableName);
				migrationRecord.setTimestamp(new Timestamp(System.currentTimeMillis()));
				migrationRecord.setState(MigrationState.FAILED);
				migrationRepository.save(migrationRecord);
			}
		}
	}
}
