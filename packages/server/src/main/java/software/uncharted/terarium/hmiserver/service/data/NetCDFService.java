package software.uncharted.terarium.hmiserver.service.data;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.NetCDF;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.unidata.io.InMemoryRandomAccessFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class NetCDFService extends S3BackedAssetService<NetCDF> {
	public NetCDFService(final ElasticsearchConfiguration elasticConfig, final Config config, final ElasticsearchService elasticService, final S3ClientService s3ClientService) {
		super(elasticConfig, config, elasticService, s3ClientService, NetCDF.class);
	}

	@Override
	protected String getAssetPath() {
		return config.getNetcdfPath();
	}

	@Override
	protected String getAssetIndex() {
		return elasticConfig.getNetCDFIndex();
	}


	/* TODO: Figure out how to extract the global attributes from a Stream */
	public NetCDF decodeNCFile(NetCDF netCDF, InputStream content) {
		try {
//		try (NetcdfFile ncfile = NetcdfFiles.open("/Users/ccoleman/Downloads/prra_Omon_IPSL-CM6A-LR_historical_r22i1p1f1_gr_185001-201412.nc")) {
			InMemoryRandomAccessFile raf = new InMemoryRandomAccessFile("stream", content.readAllBytes());
			NetcdfFile ncFile = NetcdfFiles.open(raf, "stream", null, null);
			ImmutableList<Attribute> globalAttributes = ncFile.getGlobalAttributes();
			for (Attribute attribute:globalAttributes) {
				String name = attribute.getName();
				Array values = attribute.getValues();
//				log.info("[{},{}]", name, values);
			}
		} catch (IOException ioe) {
//			log.error("Error reading NetCDF file!", ioe);
		}
		return netCDF;
	}
}
