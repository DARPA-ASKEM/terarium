package software.uncharted.terarium.hmiserver.service.data;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.NetCDF;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.unidata.io.InMemoryRandomAccessFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetCDFService {
	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

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

	public NetCDF decodeNCFile(NetCDF netCDF, InputStream content) {
		try {
//		try (NetcdfFile ncfile = NetcdfFiles.open("/Users/ccoleman/Downloads/prra_Omon_IPSL-CM6A-LR_historical_r22i1p1f1_gr_185001-201412.nc")) {
			InMemoryRandomAccessFile raf = new InMemoryRandomAccessFile("stream", content.readAllBytes());
			NetcdfFile ncFile = NetcdfFiles.open(raf, "stream", null, null);
			ImmutableList<Attribute> globalAttributes = ncFile.getGlobalAttributes();
			for (Attribute attribute:globalAttributes) {
				String name = attribute.getName();
				Array values = attribute.getValues();
				log.info("[{},{}]", name, values);
			}
		} catch (IOException ioe) {
			log.error("Error reading NetCDF file!", ioe);
		}
		return netCDF;
	}

	private String getPath(UUID id, String fileName) {
		return String.join("/", config.getNetcdfPath(), id.toString(), fileName);
	}

	public PresignedURL getUploadUrl(UUID id, String fileName) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
			config.getFileStorageS3BucketName(),
			getPath(id, fileName),
			HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}
}
