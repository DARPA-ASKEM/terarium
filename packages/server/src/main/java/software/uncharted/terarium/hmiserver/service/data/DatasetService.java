package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import io.micrometer.observation.annotation.Observed;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.repository.data.DatasetRepository;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.gollm.EmbeddingService;
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
		final EmbeddingService embeddingService,
		final Environment env,
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
			embeddingService,
			env,
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
		// If the columns are already set, don't add them again (happens when copying a dataset to another project).
		if (asset.getColumns() == null || asset.getColumns().isEmpty()) {
			extractColumns(asset);
		}
		verifyColumnRelationship(asset);
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
			verifyColumnRelationship(asset);
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
		verifyColumnRelationship(asset);
		return super.updateAsset(asset, projectId, hasWritePermission);
	}

	// This method throws if it can't get the files
	@Observed(name = "function_profile")
	public Dataset extractColumnsFromFiles(final Dataset dataset) throws IOException {
		if (dataset.getColumns() != null && !dataset.getColumns().isEmpty()) {
			return dataset;
		}
		return extractColumns(dataset);
	}

	/**
	 * Extract the columns from the dataset files.
	 *
	 * @param dataset the dataset to extract the columns from
	 * @return the dataset with the columns extracted
	 * @throws IOException if the files cannot be read
	 */
	@Observed(name = "function_profile")
	private Dataset extractColumns(final Dataset dataset) throws IOException {
		if (dataset.getFileNames() != null) {
			for (final String filename : dataset.getFileNames()) {
				if (filename.endsWith(Dataset.NC_EXTENSION)) {
					continue;
				}
				final CSVParser csvParser = getCSVFileParser(filename, dataset.getId());
				if (csvParser == null) continue;
				final List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());
				addDatasetColumns(dataset, filename, headers);
			}
		}
		return dataset;
	}

	/**
	 * Add the columns to the dataset.
	 *
	 * @param dataset  the dataset to add the columns to
	 * @param fileName the name of the file
	 * @param headers  the headers to add
	 */
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

	/**
	 * Get the CSV file parser for the given file name and dataset id.
	 *
	 * @param filename  the name of the file to parse
	 * @param datasetId the id of the dataset
	 * @return the CSV file parser
	 * @throws IOException if the file cannot be read
	 */
	@Observed(name = "function_profile")
	public CSVParser getCSVFileParser(final String filename, final UUID datasetId) throws IOException {
		final Optional<byte[]> bytes = fetchFileAsBytes(datasetId, filename);
		if (bytes.isEmpty()) {
			return null;
		}

		final Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes.get()));
		return new CSVParser(
			reader,
			CSVFormat.Builder.create(CSVFormat.DEFAULT)
				.setAllowMissingColumnNames(true)
				.setHeader()
				.setSkipHeaderRecord(false)
				.build()
		);
	}

	/**
	 * Verify that the columns have the correct relationship to the dataset. This is
	 * needed
	 * because the columns are not directly related to the dataset in the database
	 * by default.
	 *
	 * @param asset the dataset to verify
	 */
	private static void verifyColumnRelationship(final Dataset asset) {
		Optional.ofNullable(asset.getColumns()).ifPresent(columns -> columns.forEach(column -> column.setDataset(asset)));
	}

	/**
	 * Calculate the statistics for the columns in a CSV.
	 *
	 * @param csv the CSV to calculate the statistics for
	 * @return the statistics for the columns
	 */
	public static List<CsvColumnStats> calculateColumnStatistics(final List<List<String>> csv) {
		final List<CsvColumnStats> stats = new ArrayList<>();
		csv.get(0).forEach(column -> stats.add(getStats(getColumn(csv, csv.get(0).indexOf(column)))));
		return stats;
	}

	/**
	 * Get a column from a CSV matrix.
	 *
	 * @param matrix       the matrix to get the column from
	 * @param columnNumber the number of the column to get
	 * @return the column
	 */
	private static List<String> getColumn(final List<List<String>> matrix, final int columnNumber) {
		return matrix
			.stream()
			.filter(strings -> strings.size() > columnNumber)
			.map(strings -> strings.get(columnNumber))
			.collect(Collectors.toList());
	}

	/**
	 * Given a column and an amount of bins creates a CsvColumnStats object.
	 *
	 * @param aCol column to get stats for
	 * @return CsvColumnStats object
	 */
	private static CsvColumnStats getStats(final List<String> aCol) {
		final List<Integer> bins = new ArrayList<>();
		try {
			final List<Double> numberList = aCol.stream().map(Double::valueOf).collect(Collectors.toList());
			Collections.sort(numberList);
			final double minValue = numberList.get(0);
			final double maxValue = numberList.get(numberList.size() - 1);
			final double meanValue = Stats.meanOf(numberList);
			final double medianValue = Quantiles.median().compute(numberList);
			final double sdValue = Stats.of(numberList).populationStandardDeviation();
			final int binCount = 10;
			// Set up bins
			for (int i = 0; i < binCount; i++) {
				bins.add(0);
			}
			final double stepSize = (numberList.get(numberList.size() - 1) - numberList.get(0)) / (binCount - 1);

			// Fill bins:
			for (final Double aDouble : numberList) {
				final int index = (int) Math.abs(Math.floor((aDouble - numberList.get(0)) / stepSize));
				final Integer value = bins.get(index);
				bins.set(index, value + 1);
			}

			return new CsvColumnStats(bins, minValue, maxValue, meanValue, medianValue, sdValue);
		} catch (final Exception e) {
			// Cannot convert column to double, just return empty list.
			return new CsvColumnStats(bins, 0, 0, 0, 0, 0);
		}
	}
}
