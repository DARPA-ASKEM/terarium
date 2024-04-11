package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
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

  public List<DecapodesConfiguration> getDecapodesConfigurations(Integer page, Integer pageSize)
      throws IOException {

    final SearchRequest req =
        new SearchRequest.Builder()
            .index(elasticConfig.getDecapodesConfigurationIndex())
            .size(pageSize)
            .query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
            .sort(
                new SortOptions.Builder()
                    .field(new FieldSort.Builder().field("timestamp").order(SortOrder.Asc).build())
                    .build())
            .build();

    return elasticService.search(req, DecapodesConfiguration.class);
  }

  public Optional<DecapodesConfiguration> getDecapodesConfiguration(UUID id) throws IOException {
    DecapodesConfiguration doc =
        elasticService.get(
            elasticConfig.getDecapodesConfigurationIndex(),
            id.toString(),
            DecapodesConfiguration.class);
    if (doc != null && doc.getDeletedOn() == null) {
      return Optional.of(doc);
    }
    return Optional.empty();
  }

  public void deleteDecapodesConfiguration(UUID id) throws IOException {
    Optional<DecapodesConfiguration> decapodesConfiguration = getDecapodesConfiguration(id);
    if (decapodesConfiguration.isEmpty()) {
      return;
    }
    decapodesConfiguration.get().setDeletedOn(Timestamp.from(Instant.now()));
    updateDecapodesConfiguration(decapodesConfiguration.get());
  }

  public DecapodesConfiguration createDecapodesConfiguration(
      DecapodesConfiguration decapodesConfiguration) throws IOException {
    decapodesConfiguration.setCreatedOn(Timestamp.from(Instant.now()));
    elasticService.index(
        elasticConfig.getDecapodesConfigurationIndex(),
        decapodesConfiguration.setId(UUID.randomUUID()).getId().toString(),
        decapodesConfiguration);
    return decapodesConfiguration;
  }

  public Optional<DecapodesConfiguration> updateDecapodesConfiguration(
      DecapodesConfiguration decapodesConfiguration) throws IOException {
    if (!elasticService.contains(
        elasticConfig.getDecapodesConfigurationIndex(),
        decapodesConfiguration.getId().toString())) {
      return Optional.empty();
    }
    decapodesConfiguration.setUpdatedOn(Timestamp.from(Instant.now()));
    elasticService.index(
        elasticConfig.getDecapodesConfigurationIndex(),
        decapodesConfiguration.getId().toString(),
        decapodesConfiguration);
    return Optional.of(decapodesConfiguration);
  }
}
