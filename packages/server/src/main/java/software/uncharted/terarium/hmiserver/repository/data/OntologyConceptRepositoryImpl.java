package software.uncharted.terarium.hmiserver.repository.data;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.ConceptFacetSearchResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.OntologyConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.QActiveConcept;
import software.uncharted.terarium.hmiserver.models.dataservice.concept.QOntologyConcept;

@Component
public class OntologyConceptRepositoryImpl implements OntologyConceptRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	@Lazy
	private JPAQueryFactory queryFactory;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}

	@Override
	public ConceptFacetSearchResponse facetQuery(List<TaggableType> types, List<String> curies) {
		QOntologyConcept ontologyConcept = QOntologyConcept.ontologyConcept;
		QActiveConcept activeConcept = QActiveConcept.activeConcept;

		JPAQuery<OntologyConcept> query = queryFactory.selectFrom(ontologyConcept);

		if (types != null && types.size() > 0) {
			query = query.where(ontologyConcept.type.in(types));
		}

		if (curies != null && curies.size() > 0) {
			query = query.where(ontologyConcept.curie.in(curies));
		}

		ConceptFacetSearchResponse response = new ConceptFacetSearchResponse();

		ConceptFacetSearchResponse.Facets facets = new ConceptFacetSearchResponse.Facets();
		facets.setConcepts(new HashMap<String, ConceptFacetSearchResponse.Concept>());
		facets.setTypes(new HashMap<TaggableType, Long>());

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
			Long count = row.get(1, Long.class);

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
				.leftJoin(activeConcept)
				.on(ontologyConcept.curie.eq(activeConcept.curie))
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
		List<Tuple> activeConceptResults = query.select(ontologyConcept, activeConcept.name)
				.leftJoin(activeConcept)
				.on(ontologyConcept.curie.eq(activeConcept.curie))
				.groupBy(ontologyConcept.id)
				.fetch();

		List<ConceptFacetSearchResponse.Result> results = new ArrayList<>();

		// convert results to ConceptFacetSearchResponse and return it
		for (Tuple row : activeConceptResults) {

			OntologyConcept concept = row.get(0, OntologyConcept.class);
			String name = row.get(1, String.class);

			ConceptFacetSearchResponse.Result result = new ConceptFacetSearchResponse.Result();
			result.setId(concept.getId());
			result.setType(concept.getType());
			result.setCurie(concept.getCurie());
			result.setName(name);

			results.add(result);
		}

		response.setResults(results);

		return response;
	}
}
