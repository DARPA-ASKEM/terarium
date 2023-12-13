package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.models.data.concept.ConceptFacetSearchResponse;
import software.uncharted.terarium.hmiserver.models.data.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.data.concept.QActiveConcept;
import software.uncharted.terarium.hmiserver.models.data.concept.QOntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;

@RequiredArgsConstructor
public class OntologyConceptRepositoryCustomImpl implements OntologyConceptRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	private final JPAQueryFactory queryFactory;

	@Override
	public ConceptFacetSearchResponse facetQuery(List<TaggableType> types, List<String> curies) {
		QOntologyConcept ontologyConcept = QOntologyConcept.ontologyConcept;
		QActiveConcept activeConcept = QActiveConcept.activeConcept;

		JPAQuery<OntologyConcept> query = queryFactory.selectFrom(ontologyConcept)
				.where(ontologyConcept.type.in(types));

		if (curies.size() > 0) {
			query = query.where(ontologyConcept.curie.in(curies));
		}

		ConceptFacetSearchResponse response = new ConceptFacetSearchResponse();

		ConceptFacetSearchResponse.Facets facets = new ConceptFacetSearchResponse.Facets();

		/*
		 * SELECT type, COUNT(DISTINCT type, object_id)
		 * FROM ontology_concept
		 * WHERE type IN (%s)
		 * GROUP BY type
		 */
		List<Tuple> typeCounts = query.select(ontologyConcept.type, ontologyConcept.objectId.countDistinct())
				.groupBy(ontologyConcept.type)
				.fetch();

		for (Tuple row : typeCounts) {

			TaggableType type = row.get(0, TaggableType.class);
			Integer count = row.get(1, Integer.class);

			facets.getTypes().put(type, count);
		}

		/*
		 * SELECT oc.curie, ac.name, COUNT(oc.curie)
		 * FROM ontology_concept oc
		 * LEFT JOIN active_concept ac ON oc.curie = ac.curie
		 * WHERE oc.type IN (%s)
		 * GROUP BY oc.curie, ac.name
		 */
		List<Tuple> conceptCounts = query.select(ontologyConcept.curie, activeConcept.name, ontologyConcept.count())
				.leftJoin(activeConcept).on(ontologyConcept.curie.eq(activeConcept.curie))
				.groupBy(ontologyConcept.curie, activeConcept.name)
				.fetch();

		for (Tuple row : conceptCounts) {

			String curie = row.get(0, String.class);

			ConceptFacetSearchResponse.Concept concept = new ConceptFacetSearchResponse.Concept();
			concept.setName(row.get(1, String.class));
			concept.setCount(row.get(2, Long.class));

			facets.getConcepts().put(curie, concept);
		}

		response.setFacets(facets);

		/*
		 * SELECT oc.*, ac.name
		 * FROM ontology_concept oc
		 * JOIN active_concept ac ON oc.curie = ac.curie
		 * WHERE oc.type IN (%s)
		 */
		List<Tuple> results = query.select(ontologyConcept, activeConcept.name)
				.leftJoin(activeConcept).on(ontologyConcept.curie.eq(activeConcept.curie))
				.fetch();

		// convert results to ConceptFacetSearchResponse and return it
		for (Tuple row : results) {

			OntologyConcept concept = row.get(0, OntologyConcept.class);
			String name = row.get(1, String.class);

			ConceptFacetSearchResponse.Result result = new ConceptFacetSearchResponse.Result();
			result.setId(concept.getId());
			result.setType(concept.getType());
			result.setCurie(concept.getCurie());
			result.setName(name);

			response.getResults().add(result);
		}

		return response;
	}

}
