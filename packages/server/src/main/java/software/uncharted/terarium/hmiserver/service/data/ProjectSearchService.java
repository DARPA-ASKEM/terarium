package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.HasChildQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectSearchService {

	protected final ElasticsearchConfiguration elasticConfig;
	protected final ElasticsearchService elasticService;

	protected String getIndex() {
		log.info("Project index: {}", elasticConfig.getProjectIndex());
		return elasticConfig.getProjectIndex();
	}

	public String getAlias() {
		log.info("Project alias: {}", elasticConfig.getProjectAlias());
		return elasticConfig.getProjectAlias();
	}

	public String getCurrentAssetIndex() throws IOException {
		return elasticService.getIndexFromAlias(getAlias());
	}

	@Data
	public static class PermissionJoin {

		private String name = null;
		private UUID parent = null;

		static PermissionJoin CreateProjectJoin() {
			final PermissionJoin join = new PermissionJoin();
			join.setName("project");
			join.setParent(null);
			return join;
		}

		static PermissionJoin CreatePermissionJoin(final UUID projectId) {
			final PermissionJoin join = new PermissionJoin();
			join.setName("permission");
			join.setParent(projectId);
			return join;
		}
	}

	@Data
	public static class ProjectDocument {

		private final String type = "project";
		private String userId; // the user id of who owns the project
		private Boolean publicAsset; // whether the project is public or not
		private Timestamp createdOn;
		private Timestamp updatedOn;

		// join field properties
		private PermissionJoin permissionJoin;
	}

	@Data
	public static class ProjectPermissionDocument {

		private final String type = "permission";
		private String userId; // the user id who holds the permission

		// join field properties
		private PermissionJoin permissionJoin;
	}

	/**
	 * Setup the index and alias for the asset this service manages and ensure it is
	 * empty
	 *
	 * @throws IOException If there is an error setting up the index and alias
	 */
	public void setupIndexAndAliasAndEnsureEmpty() throws IOException {
		log.info("Setting up index {} and alias {}", getIndex(), getAlias());
		final String index = getIndex();
		try {
			final String currentIndex = getCurrentAssetIndex();
			// if another index exists, delete it.
			if (!currentIndex.equals(index)) {
				elasticService.deleteIndex(currentIndex);
			}
		} catch (final Exception e) {}
		elasticService.createOrEnsureIndexIsEmpty(index);
		elasticService.createAlias(index, getAlias());
	}

	/**
	 * Teardown the index and alias for the asset this service manages
	 *
	 * @throws IOException If there is an error tearing down the index and alias
	 */
	public void teardownIndexAndAlias() throws IOException {
		log.info("Tearing down index {}", getIndex());
		final String index = getCurrentAssetIndex();
		elasticService.deleteIndex(index);
	}

	public void indexProject(final Project project) throws IOException {
		final ProjectDocument doc = new ProjectDocument();
		doc.setUserId(project.getUserId());
		doc.setPublicAsset(project.getPublicAsset());
		doc.setCreatedOn(new Timestamp(System.currentTimeMillis()));
		doc.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		doc.setPermissionJoin(PermissionJoin.CreateProjectJoin());

		final String routing = project.getId().toString();

		elasticService.indexWithRouting(getAlias(), project.getId().toString(), doc, routing);
	}

	public void updateProject(final Project project) throws IOException {
		final ProjectDocument doc = new ProjectDocument();
		doc.setUserId(project.getUserId());
		doc.setPublicAsset(project.getPublicAsset());
		doc.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
		doc.setPermissionJoin(PermissionJoin.CreateProjectJoin());

		elasticService.update(getAlias(), project.getId().toString(), doc);
	}

	public void removeProject(final UUID id) throws IOException {
		elasticService.delete(getAlias(), id.toString());
	}

	private String getPermissionId(final UUID projectId, final String userId) {
		return projectId.toString() + "_" + userId;
	}

	public void addProjectPermission(final UUID projectId, final String userId) throws IOException {
		final ProjectPermissionDocument doc = new ProjectPermissionDocument();
		doc.setUserId(userId);
		doc.setPermissionJoin(PermissionJoin.CreatePermissionJoin(projectId));

		final String routing = projectId.toString();

		elasticService.indexWithRouting(getAlias(), getPermissionId(projectId, userId), doc, routing);
	}

	public void removeProjectPermission(final UUID projectId, final String userId) throws IOException {
		elasticService.delete(getAlias(), getPermissionId(projectId, userId));
	}

	public List<ProjectDocument> searchProjectsForUser(
		final String userId,
		final Integer from,
		final Integer pageSize,
		final Query query
	) throws IOException {
		final SearchRequest.Builder req = new SearchRequest.Builder()
			.index(getAlias())
			.from(from)
			.size(pageSize)
			.sort(
				new SortOptions.Builder()
					.field(new FieldSort.Builder().field("updatedOn").order(SortOrder.Desc).build())
					.build()
			);

		// Does this user have permission to see this project?
		final HasChildQuery hasChildQuery = HasChildQuery.of(h ->
			h.type("permission").query(QueryBuilders.term(t -> t.field("userId").value(userId)))
		);

		// Is the project public, does the user own it, or does the user have read
		// access?
		final Query permissionQuery = QueryBuilders.bool(b2 ->
			b2
				.should(sh -> sh.term(t -> t.field("publicAsset").value(true)))
				.should(sh -> sh.term(t -> t.field("userId").value(userId)))
				.should(sh -> sh.hasChild(hasChildQuery))
				.minimumShouldMatch("1")
		);

		// Only return proejcts, not permissions
		final Query onlyProjects = QueryBuilders.bool(b2 -> b2.must(sh -> sh.term(t -> t.field("type").value("project"))));

		if (query != null) {
			req.query(q -> q.bool(b -> b.must(permissionQuery).must(onlyProjects).must(query)));
		} else {
			req.query(q -> q.bool(b -> b.must(onlyProjects).must(permissionQuery)));
		}
		return elasticService.search(req.build(), ProjectDocument.class);
	}
}
