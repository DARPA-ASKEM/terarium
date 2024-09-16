package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.repository.data.DatasetRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Slf4j
@Service
public class DatasetService extends TerariumAssetServiceWithSearch<Dataset, DatasetRepository> {

	public DatasetService(
		final ObjectMapper objectMapper,
		final Config config,
		final ElasticsearchConfiguration elasticConfig,
		final ElasticsearchService elasticService,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final S3ClientService s3ClientService,
		final DatasetRepository repository
	) {
		super(
			objectMapper,
			config,
			elasticConfig,
			elasticService,
			projectService,
			projectAssetService,
			s3ClientService,
			repository,
			Dataset.class
		);
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		return config.getDatasetPath();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetIndex() {
		return elasticConfig.getDatasetIndex();
	}

	@Override
	public String getAssetAlias() {
		return elasticConfig.getDatasetAlias();
	}

	@Override
	@Observed(name = "function_profile")
	public Dataset createAsset(final Dataset asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		extractColumnsForFreshCreate(asset);

		if (asset.getColumns() != null) {
			for (final DatasetColumn column : asset.getColumns()) {
				column.setDataset(asset);
			}
		}
		return super.createAsset(asset, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public List<Dataset> createAssets(
		final List<Dataset> assets,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException {
		for (final Dataset asset : assets) {
			if (asset.getColumns() != null) {
				for (final DatasetColumn column : asset.getColumns()) {
					column.setDataset(asset);
				}
			}
		}

		return super.createAssets(assets, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<Dataset> updateAsset(
		final Dataset asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		if (asset.getColumns() != null) {
			for (final DatasetColumn column : asset.getColumns()) {
				column.setDataset(asset);
			}
		}
		return super.updateAsset(asset, projectId, hasWritePermission);
	}

	// This method throws if it can't get the files
	@Observed(name = "function_profile")
	public Dataset extractColumnsFromFiles(final Dataset dataset) throws IOException {
		if (dataset.getColumns() != null && !dataset.getColumns().isEmpty()) {
			// columns are set. No need to extract
			return dataset;
		}
		if (dataset.getFileNames() != null || dataset.getFileNames().isEmpty()) {
			// no file names to extract columns from
			return dataset;
		}

		for (final String filename : dataset.getFileNames()) {
			if (!filename.endsWith(".nc")) {
				final List<List<String>> csv = getCSVFile(filename, dataset.getId(), 1);
				if (csv == null || csv.isEmpty()) {
					continue;
				}
				addDatasetColumns(dataset, filename, csv.get(0));
			}
		}

		return dataset;
	}

	@Observed(name = "function_profile")
	private Dataset extractColumnsForFreshCreate(final Dataset dataset) throws IOException {
		if (dataset.getFileNames() != null || dataset.getFileNames().isEmpty()) {
			// no file names to extract columns from
			return dataset;
		}

		for (final String filename : dataset.getFileNames()) {
			if (!filename.endsWith(".nc")) {
				try {
					final List<List<String>> csv = getCSVFile(filename, dataset.getId(), 1);
					if (csv == null || csv.isEmpty()) {
						continue;
					}
					addDatasetColumns(dataset, filename, csv.get(0));
				} catch (final IOException e) {
					// if file is not available yet, don't continue gracefully
					continue;
				}
			}
		}

		return dataset;
	}

	@Observed(name = "function_profile")
	public static void addDatasetColumns(final Dataset dataset, final String fileName, final List<String> headers) {
		if (dataset.getColumns() == null) {
			dataset.setColumns(new ArrayList<>());
		}
		for (final String header : headers) {
			final DatasetColumn column = new DatasetColumn()
				.setName(header)
				.setFileName(fileName)
				.setAnnotations(new ArrayList<>());
			column.setDataset(dataset);
			dataset.getColumns().add(column);
		}
	}

	@Observed(name = "function_profile")
	public List<List<String>> getCSVFile(final String filename, final UUID datasetId, final Integer limit)
		throws IOException {
		String rawCSV = "";

		final Optional<byte[]> bytes = fetchFileAsBytes(datasetId, filename);
		if (bytes.isEmpty()) {
			return null;
		}

		final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes.get())));

		String line;
		Integer count = 0;
		while ((line = reader.readLine()) != null) {
			if (limit > 0 && count > limit) {
				break;
			}
			rawCSV += line + '\n';
			count++;
		}

		final List<List<String>> csv;
		csv = csvToRecords(rawCSV);
		return csv;
	}

	@Observed(name = "function_profile")
	private static List<List<String>> csvToRecords(final String rawCsvString) throws IOException {
		final List<List<String>> records = new ArrayList<>();
		try (final CSVParser parser = new CSVParser(new StringReader(rawCsvString), CSVFormat.DEFAULT)) {
			for (final CSVRecord csvRecord : parser) {
				final List<String> values = new ArrayList<>();
				csvRecord.forEach(values::add);
				records.add(values);
			}
		}
		return records;
	}
}
