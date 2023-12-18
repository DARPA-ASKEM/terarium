package software.uncharted.terarium.hmiserver.controller.dataservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.math.Quantiles;
import com.google.common.math.Stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseStatus;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;

@RequestMapping("/datasets")
@RestController
@Slf4j
public class DatasetController {

	private static final int DEFAULT_CSV_LIMIT = 100;

	@Autowired
	DatasetService datasetService;

	@Autowired
	JsDelivrProxy githubProxy;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all datasets")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Datasets found.", content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class)))),
			@ApiResponse(responseCode = "204", description = "There are no datasets found and no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving datasets from the data store", content = @Content)
	})
	public ResponseEntity<List<Dataset>> getDatasets(
			@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
			@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {
		try {
			return ResponseEntity.ok(datasetService.getDatasets(page, pageSize));
		} catch (IOException e) {
			final String error = "Unable to get datasets";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new dataset")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Dataset created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the dataset", content = @Content)
	})
	public ResponseEntity<Dataset> createDataset(@RequestBody Dataset dataset) {

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(datasetService.createDataset(dataset));
		} catch (IOException e) {
			final String error = "Unable to create dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets dataset by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dataset found.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = DocumentAsset.class))),
			@ApiResponse(responseCode = "204", description = "There was no dataset found but no errors occurred", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the dataset from the data store", content = @Content)
	})
	public ResponseEntity<Dataset> getDataset(@PathVariable("id") final UUID id) {
		try {
			return ResponseEntity.ok(datasetService.getDataset(id));
		} catch (IOException e) {
			final String error = "Unable to create dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a dataset")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Delete dataset", content = {
					@Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)) }),
			@ApiResponse(responseCode = "404", description = "Dataset could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
	})
	public ResponseEntity<ResponseDeleted> deleteDocument(
			@PathVariable("id") final UUID id) {

		try {
			datasetService.deleteDataset(id);
			return ResponseEntity.ok(new ResponseDeleted("Dataset", id));
		} catch (IOException e) {
			final String error = "Unable to delete dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a dataset")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dataset updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the dataset", content = @Content)
	})
	ResponseEntity<Dataset> updateDataset(
			@PathVariable("id") UUID id,
			@RequestBody Dataset dataset) {

		try {
			final Optional<Dataset> updated = datasetService.updateDataset(dataset.setId(id));
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (IOException e) {
			final String error = "Unable to delete dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	@ExceptionHandler
	@org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
	public void handle(Exception e) {
		log.info("Returning HTTP 400 Bad Request", e);
	}

	@GetMapping("/{datasetId}/downloadCSV")
	@Secured(Roles.USER)
	@Operation(summary = "Download dataset CSV")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dataset CSV.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CsvAsset.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue retrieving the dataset from the data store", content = @Content)
	})
	public ResponseEntity<CsvAsset> getCsv(
			@PathVariable("datasetId") final UUID datasetId,
			@RequestParam("filename") final String filename,
			@RequestParam(name = "limit", defaultValue = "-1", required = false) final Integer limit // -1 means no
																										// limit
	) {

		String rawCSV = "";
		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			final PresignedURL presignedURL = datasetService.getDownloadUrl(datasetId, filename);
			final HttpGet get = new HttpGet(Objects.requireNonNull(presignedURL).getUrl());
			final HttpResponse response = httpclient.execute(get);
			rawCSV = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

		} catch (final Exception e) {
			final String error = "Unable to get dataset CSV";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}

		final List<List<String>> csv = csvToRecords(rawCSV);
		final List<String> headers = csv.get(0);
		final List<CsvColumnStats> csvColumnStats = new ArrayList<>();
		for (int i = 0; i < csv.get(0).size(); i++) {
			final List<String> column = getColumn(csv, i);
			csvColumnStats.add(getStats(column.subList(1, column.size()))); // remove first as it is header:
		}

		final int linesToRead = limit != null ? (limit == -1 ? csv.size() : limit) : DEFAULT_CSV_LIMIT;

		final CsvAsset csvAsset = new CsvAsset(
				csv.subList(0, Math.min(linesToRead + 1, csv.size())),
				csvColumnStats,
				headers,
				csv.size());

		return ResponseEntity.ok(csvAsset);
	}

	/**
	 * Downloads a CSV file from github given the path and owner name, then uploads
	 * it to the dataset.
	 */
	@PutMapping("/{datasetId}/uploadCSVFromGithub")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a CSV file from github to a dataset")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Uploaded the CSV file.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the CSV", content = @Content)
	})
	public ResponseEntity<ResponseStatus> uploadCsvFromGithub(
			@PathVariable("datasetId") final UUID datasetId,
			@RequestParam("path") final String path,
			@RequestParam("repoOwnerAndName") final String repoOwnerAndName,
			@RequestParam("filename") final String filename) {

		log.debug("Uploading CSV file from github to dataset {}", datasetId);

		// download CSV from github
		final String csvString = githubProxy.getGithubCode(repoOwnerAndName, path).getBody();

		final HttpEntity csvEntity = new StringEntity(csvString, ContentType.APPLICATION_OCTET_STREAM);
		final String[] csvRows = csvString.split("\\R");
		final String[] headers = csvRows[0].split(",");
		return uploadCSVAndUpdateColumns(datasetId, filename, csvEntity, headers);
	}

	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS
	 * then push
	 * the file to S3.
	 *
	 * @param datasetId ID of the dataset to upload t
	 * @param filename  CSV file to upload
	 * @return Response
	 */
	@PutMapping(value = "/{datasetId}/uploadCSV", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a CSV file to a dataset")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Uploaded the CSV file.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class))),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the CSV", content = @Content)
	})
	public ResponseEntity<ResponseStatus> uploadCsv(
			@PathVariable("datasetId") final UUID datasetId,
			@RequestParam("filename") final String filename,
			@RequestPart("file") final MultipartFile input) {

		try {
			log.debug("Uploading CSV file to dataset {}", datasetId);

			final byte[] csvBytes = input.getBytes();

			final HttpEntity csvEntity = new ByteArrayEntity(csvBytes, ContentType.APPLICATION_OCTET_STREAM);
			final String csvString = new String(csvBytes);
			final String[] csvRows = csvString.split("\\R");
			final String[] headers = csvRows[0].split(",");
			return uploadCSVAndUpdateColumns(datasetId, filename, csvEntity, headers);
		} catch (IOException e) {
			final String error = "Unable to delete dataset";
			log.error(error, e);
			throw new ResponseStatusException(
					org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
					error);
		}
	}

	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS
	 * then push
	 * the file to S3 via a presigned URL and update the dataset with the headers.
	 * <p>
	 * If the headers fail to update there will be a print to the log, however, the
	 * response will
	 * just be the status of the original csv upload.
	 *
	 * @param datasetId ID of the dataset to upload to
	 * @param fileName  CSV file to upload
	 * @param csvEntity CSV file as an HttpEntity
	 * @param headers   headers of the CSV file
	 * @return Response from the upload
	 */
	private ResponseEntity<ResponseStatus> uploadCSVAndUpdateColumns(final UUID datasetId, final String fileName,
			final HttpEntity csvEntity, final String[] headers) {

		try (final CloseableHttpClient httpclient = HttpClients.custom()
				.disableRedirectHandling()
				.build()) {

			// upload CSV to S3
			final PresignedURL presignedURL = datasetService.getUploadUrl(datasetId, fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(csvEntity);
			final HttpResponse response = httpclient.execute(put);
			int status = response.getStatusLine().getStatusCode();

			// update dataset with headers if the previous upload was successful
			if (status == HttpStatus.OK.value()) {
				log.debug("Successfully uploaded CSV file to dataset {}. Now updating TDS with headers", datasetId);

				final List<DatasetColumn> columns = new ArrayList<>(headers.length);
				for (final String header : headers) {
					columns.add(new DatasetColumn().setName(header).setAnnotations(new ArrayList<>()));
				}
				final Dataset updatedDataset = datasetService.getDataset(datasetId);
				if (updatedDataset == null) {
					log.error("Failed to get dataset {} after upload", datasetId);
					return ResponseEntity.internalServerError().build();
				}
				updatedDataset.setColumns(columns);

				datasetService.updateDataset(updatedDataset);
			}

			return ResponseEntity.ok(new ResponseStatus(status));

		} catch (final Exception e) {
			log.error("Unable to PUT csv data", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	// TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1005
	// warning this is not sufficient for the long term. Should likely use a library
	// for this conversion formatting may break this.
	private static List<List<String>> csvToRecords(final String rawCsvString) {
		final String[] csvRows = rawCsvString.split("\n");
		final String[] headers = csvRows[0].split(",");
		final List<List<String>> csv = new ArrayList<>();
		for (final String csvRow : csvRows) {
			csv.add(Arrays.asList(csvRow.split(",")));
		}
		return csv;
	}

	private static List<String> getColumn(final List<List<String>> matrix, final int columnNumber) {
		final List<String> column = new ArrayList<>();
		for (final List<String> strings : matrix) {
			if (strings.size() > columnNumber) {
				column.add(strings.get(columnNumber));
			}

		}
		return column;
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
			// set up row as numbers. may fail here.
			// List<Integer> numberList = aCol.stream().map(String s ->
			// Integer.parseInt(s.trim()));
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
