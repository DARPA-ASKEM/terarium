package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.DataMigration;
import software.uncharted.terarium.hmiserver.models.DataMigration.MigrationState;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.repository.DataMigrationRepository;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;
import software.uncharted.terarium.hmiserver.repository.data.WorkflowRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

/**
 * Base class for services that manage TerariumAssets with syncing to
 * Elasticsearch.
 *
 * @param <T> The type of asset this service manages
 * @param <R> The respository of the asset this service manages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TerariumAssetEsToPGMigration {

	private final ElasticsearchConfiguration elasticConfig;
	private final ElasticsearchService elasticService;

	private final DataMigrationRepository migrationRepository;

	private final WorkflowService workflowService;

	@PersistenceContext
	private EntityManager entityManager;

	private String getTableName(final Class<?> entityClass) {
		final Metamodel metamodel = entityManager.getMetamodel();
		return metamodel.entity(entityClass).getName();
	}

	@Data
	static private class MigrationConfig<T extends TerariumAsset, R extends PSCrudSoftDeleteRepository<T, UUID>> {

		final TerariumAssetServiceWithoutSearch<T, R> service;
		final String index;

		MigrationConfig(final TerariumAssetServiceWithoutSearch<T, R> service, final String index) {
			this.service = service;
			this.index = index;
		}

		void migrateFromEsToPg(final ElasticsearchService elasticService) throws IOException {
			// check if there is a target index to migrate from
			if (!elasticService.indexExists(index) || elasticService.count(index) == 0) {
				return;
			}

			// check if the data has already been migrated
			final int PAGE_SIZE = 256;
			String lastId = null;

			log.info("Migrating from ES index {} to SQL...", index);

			final String SORT_FIELD = "createdOn";

			while (true) {
				final SearchRequest.Builder reqBuilder = new SearchRequest.Builder()
						.index(index)
						.size(PAGE_SIZE)
						.sort(new SortOptions.Builder()
								.field(new FieldSort.Builder().field(SORT_FIELD).order(SortOrder.Asc).build()).build());

				if (lastId != null) {
					reqBuilder.searchAfter(FieldValue.of(SORT_FIELD), FieldValue.of(lastId));
				}

				final SearchRequest req = reqBuilder
						.build();

				final SearchResponse<T> resp = elasticService.getClient().search(req,
						service.getAssetClass());

				final String pageLastId = null;
				final List<T> assets = new ArrayList<>();
				for (final Hit<T> hit : resp.hits().hits()) {
					lastId = hit.sort().get(0).toString();

					final T asset = hit.source();
					if (asset == null) {
						log.warn("Null document payload for id: {}, skipping", hit.id());
						continue;
					}
					if (asset.getId() == null || asset.getId().toString() != hit.id()) {
						asset.setId(UUID.fromString(hit.id()));
					}
					assets.add(asset);
				}

				if (assets.size() > 0) {
					service.getRepository().saveAll(assets);
				}

				if (pageLastId == null || lastId == pageLastId || assets.size() < PAGE_SIZE) {
					break;
				}
			}
		}
	}

	List<MigrationConfig<?, ?>> getMigrations() {
		return List.of(
				new MigrationConfig<Workflow, WorkflowRepository>(workflowService, elasticConfig.getWorkflowIndex()));
	}

	@PostConstruct
	void runMigrations() {
		for (final MigrationConfig<?, ?> migration : getMigrations()) {

			final String tableName = getTableName(migration.getService().getAssetClass());

			try {
				DataMigration migrationRecord = migrationRepository.findByTableName(tableName).orElse(null);

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

				log.info("Migrated es index {} to pg table: {} succesfully", migration.getIndex(), tableName);

			} catch (final Exception e) {
				log.warn("Failed to migrate data from ES to PG for index: {}", migration.getIndex(), e);

				final DataMigration migrationRecord = new DataMigration();
				migrationRecord.setTableName(tableName);
				migrationRecord.setTimestamp(new Timestamp(System.currentTimeMillis()));
				migrationRecord.setState(MigrationState.FAILED);
				migrationRepository.save(migrationRecord);
			}
		}
	}

}
