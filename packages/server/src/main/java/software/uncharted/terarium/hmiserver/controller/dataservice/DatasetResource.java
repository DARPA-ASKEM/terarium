package software.uncharted.terarium.hmiserver.controller.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.uncharted.terarium.hmiserver.controller.SnakeCaseResource;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@RequestMapping("/datasets")
@RestController
@Slf4j
public class DatasetResource implements SnakeCaseResource {

	private static final int DEFAULT_CSV_LIMIT = 100;


	@Autowired
	DatasetProxy datasetProxy;

	@Autowired
	JsDelivrProxy githubProxy;

	@GetMapping
	public ResponseEntity<List<Dataset>> getDatasets(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page
	) {

		return ResponseEntity.ok(datasetProxy.getAssets(pageSize, page).getBody());
	}

	@PostMapping
	public ResponseEntity<JsonNode> createDataset(
		@RequestBody final Dataset dataset
	) {
		return ResponseEntity.ok(datasetProxy.createAsset(convertObjectToSnakeCaseJsonNode(dataset))).getBody();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Dataset> getDataset(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(datasetProxy.getAsset(id).getBody());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<JsonNode> deleteDataset(
		@PathVariable("id") final String id
	) {
		return ResponseEntity.ok(datasetProxy.deleteAsset(id).getBody());
	}

	@PutMapping("/{id}")
	public ResponseEntity<JsonNode> updateDataset(
		@PathVariable("id") final String id,
		@RequestBody final Dataset dataset
	) {
		return ResponseEntity.ok(datasetProxy.updateAsset(id, convertObjectToSnakeCaseJsonNode(dataset)).getBody());
	}


	@GetMapping("/{datasetId}/downloadCSV")
	public ResponseEntity<CsvAsset> getCsv(
		@PathVariable("datasetId") final String datasetId,
		@RequestParam("filename") final String filename,
		@RequestParam("limit") final Integer limit    // -1 means no limit
	) throws IOException {

		log.debug("Getting CSV content");

		String rawCSV = "";

		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			final ResponseEntity<PresignedURL> presignedURL = datasetProxy.getDownloadUrl(datasetId, filename);
			final HttpGet get = new HttpGet(Objects.requireNonNull(presignedURL.getBody()).getUrl());
			final HttpResponse response = httpclient.execute(get);
			rawCSV = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

		} catch (Exception e) {
			log.error("Unable to GET csv data", e);
			return ResponseEntity.internalServerError().build();
		}

		List<List<String>> csv = csvToRecords(rawCSV);
		List<String> headers = csv.get(0);
		List<CsvColumnStats> csvColumnStats = new ArrayList<>();
		for (int i = 0; i < csv.get(0).size(); i++) {
			List<String> column = getColumn(csv, i);
			csvColumnStats.add(getStats(column.subList(1, column.size()))); //remove first as it is header:
		}

		final int linesToRead = limit != null ? limit == -1 ? csv.size() : limit : DEFAULT_CSV_LIMIT;

		CsvAsset csvAsset = new CsvAsset(csv.subList(0, Math.min(linesToRead, csv.size() - 1)), csvColumnStats, headers, csv.size());
		return ResponseEntity.ok(csvAsset);
	}

	/**
	 * Downloads a CSV file from github given the path and owner name, then uploads it to the dataset.
	 */
	@PutMapping("/{datasetId}/uploadCSVFromGithub")
	public ResponseEntity<JsonNode> uploadCsvFromGithub(
		@PathVariable("datasetId") final String datasetId,
		@RequestParam("path") final String path,
		@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
		@RequestParam("filename") final String filename
	) {
		log.debug("Uploading CSV file from github to dataset {}", datasetId);


		//download CSV from github
		String csvString = githubProxy.getGithubCode(repoOwnerAndName, path).getBody();

		HttpEntity csvEntity = new StringEntity(csvString, ContentType.APPLICATION_OCTET_STREAM);
		String[] csvRows = csvString.split("\\R");
		String[] headers = csvRows[0].split(",");
		return uploadCSVAndUpdateColumns(datasetId, filename, csvEntity, headers);

	}


	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS then push
	 * the file to S3.
	 *
	 * @param datasetId ID of the dataset to upload t
	 * @param filename  CSV file to upload
	 * @return Response
	 */
	@PutMapping(value = "/{datasetId}/uploadCSV", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<JsonNode> uploadCsv(
		@PathVariable("datasetId") final String datasetId,
		@RequestParam("filename") final String filename,
		@RequestPart("file") MultipartFile input
	) throws IOException {

		log.debug("Uploading CSV file to dataset {}", datasetId);
		int status;
		byte[] csvBytes = input.getBytes();


		HttpEntity csvEntity = new ByteArrayEntity(csvBytes, ContentType.APPLICATION_OCTET_STREAM);
		String csvString = new String(csvBytes);
		String[] csvRows = csvString.split("\\R");
		String[] headers = csvRows[0].split(",");
		return uploadCSVAndUpdateColumns(datasetId, filename, csvEntity, headers);

	}

	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS then push
	 * the file to S3 via a presigned URL and update the dataset with the headers.
	 * <p>
	 * If the headers fail to update there will be a print to the log, however, the response will
	 * just be the status of the original csv upload.
	 *
	 * @param datasetId ID of the dataset to upload to
	 * @param fileName  CSV file to upload
	 * @param csvEntity CSV file as an HttpEntity
	 * @param headers   headers of the CSV file
	 * @return Response from the upload
	 */
	private ResponseEntity<JsonNode> uploadCSVAndUpdateColumns(String datasetId, String fileName, HttpEntity csvEntity, String[] headers) {
		int status;
		try (CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload CSV to S3
			final PresignedURL presignedURL = datasetProxy.getUploadUrl(datasetId, fileName).getBody();
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(csvEntity);
			final HttpResponse response = httpclient.execute(put);
			status = response.getStatusLine().getStatusCode();

			// update dataset with headers if the previous upload was successful
			if (status == HttpStatus.OK.value()) {
				log.debug("Successfully uploaded CSV file to dataset {}. Now updating TDS with headers", datasetId);

				List<DatasetColumn> columns = new ArrayList<>(headers.length);
				for (String header : headers) {
					columns.add(new DatasetColumn().setName(header).setAnnotations(new ArrayList<>()));
				}
				Dataset updatedDataset = datasetProxy.getAsset(datasetId).getBody();
				if (updatedDataset == null) {
					log.error("Failed to get dataset {} after upload", datasetId);
					return ResponseEntity.internalServerError().build();
				}
				updatedDataset.setColumns(columns);
				ResponseEntity<JsonNode> r = datasetProxy.updateAsset(datasetId, convertObjectToSnakeCaseJsonNode(updatedDataset));
				if (r.getStatusCode().value() != HttpStatus.OK.value()) {
					log.error("Failed to update dataset {} with headers", datasetId);
				}
			}

			return ResponseEntity.status(status).build();


		} catch (Exception e) {
			log.error("Unable to PUT csv data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1005
	// warning this is not sufficient for the long term. Should likely use a library for this conversion formatting may break this.
	private List<List<String>> csvToRecords(String rawCsvString) {
		String[] csvRows = rawCsvString.split("\n");
		String[] headers = csvRows[0].split(",");
		List<List<String>> csv = new ArrayList<>();
		for (String csvRow : csvRows) {
			csv.add(Arrays.asList(csvRow.split(",")));
		}
		return csv;
	}

	private List<String> getColumn(List<List<String>> matrix, int columnNumber) {
		List<String> column = new ArrayList<>();
		for (List<String> strings : matrix) {
			if (strings.size() > columnNumber) {
				column.add(strings.get(columnNumber));
			}

		}
		return column;
	}

	/**
	 * Given a column and an amount of bins create a CsvColumnStats object.
	 *
	 * @param aCol column to get stats for
	 * @return CsvColumnStats object
	 */
	private CsvColumnStats getStats(List<String> aCol) {
		List<Integer> bins = new ArrayList<>();
		try {
			// set up row as numbers. may fail here.
			// List<Integer> numberList = aCol.stream().map(String s -> Integer.parseInt(s.trim()));
			List<Double> numberList = aCol.stream().map(Double::valueOf).collect(Collectors.toList());
			Collections.sort(numberList);
			double minValue = numberList.get(0);
			double maxValue = numberList.get(numberList.size() - 1);
			double meanValue = Stats.meanOf(numberList);
			double medianValue = Quantiles.median().compute(numberList);
			double sdValue = Stats.of(numberList).populationStandardDeviation();
			int binCount = 10;
			//Set up bins
			for (int i = 0; i < binCount; i++) {
				bins.add(0);
			}
			double stepSize = (numberList.get(numberList.size() - 1) - numberList.get(0)) / (binCount - 1);

			// Fill bins:
			for (Double aDouble : numberList) {
				int index = (int) Math.abs(Math.floor((aDouble - numberList.get(0)) / stepSize));
				Integer value = bins.get(index);
				bins.set(index, value + 1);
			}

			return new CsvColumnStats(bins, minValue, maxValue, meanValue, medianValue, sdValue);

		} catch (Exception e) {
			//Cannot convert column to double, just return empty list.
			return new CsvColumnStats(bins, 0, 0, 0, 0, 0);
		}
	}
}
