package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.NetCDF;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NetCDFService {
	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public Optional<NetCDF> getNetCDF(UUID id) throws IOException {
		NetCDF doc = elasticService.get(elasticConfig.getNetCDFIndex(), id.toString(), NetCDF.class);
		if (doc != null && doc.getDeletedOn() == null) {
			return Optional.of(doc);
		}
		return Optional.empty();
	}

	public List<NetCDF> getNetCDFs(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
			.index(elasticConfig.getNetCDFIndex())
			.from(page)
			.size(pageSize)
			.query(q -> q.bool(b -> b.mustNot(mn -> mn.exists(e -> e.field("deletedOn")))))
			.build();
		return elasticService.search(req, NetCDF.class);
	}

	public NetCDF createNetCDF(NetCDF netCDF) throws IOException {
		netCDF.setCreatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNetCDFIndex(), netCDF.setId(UUID.randomUUID()).getId().toString(),
			netCDF);
		return netCDF;
	}

	public Optional<NetCDF> updateNetCDF(NetCDF netCDF) throws IOException {
		if (!elasticService.contains(elasticConfig.getNetCDFIndex(), netCDF.getId().toString())) {
			return Optional.empty();
		}

		netCDF.setUpdatedOn(Timestamp.from(Instant.now()));
		elasticService.index(elasticConfig.getNetCDFIndex(), netCDF.getId().toString(), netCDF);
		return Optional.of(netCDF);
	}

	public void deleteNetCDF(UUID id) throws IOException {
		Optional<NetCDF> netCDF= getNetCDF(id);
		if (netCDF.isEmpty()) {
			return;
		}
		netCDF.get().setDeletedOn(Timestamp.from(Instant.now()));
		updateNetCDF(netCDF.get());
	}
}
