package software.uncharted.terarium.hmiserver.controller.dataservice;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseStatus;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.proxies.climatedata.ClimateDataProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@RequestMapping("/datasets")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DatasetController {

	private static final int DEFAULT_CSV_LIMIT = 100;

	final Config config;

	final DatasetService datasetService;
	final ClimateDataProxy climateDataProxy;

	final JsDelivrProxy githubProxy;

	final ProjectService projectService;
	final ProjectAssetService projectAssetService;
	final CurrentUserService currentUserService;
	final Messages messages;

	private final List<String> SEARCH_FIELDS = List.of("name", "description");

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all datasets")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Datasets found.",
				content = @Content(
					array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class))
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving datasets from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<List<Dataset>> getDatasets(
		@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page,
		@RequestParam(name = "terms", defaultValue = "", required = false) final String terms
	) {
		try {
			List<String> ts = new ArrayList<>();
			if (terms != null && !terms.isEmpty()) {
				ts = Arrays.asList(terms.split("[,\\s]"));
			}

			Query query = null;

			if (!ts.isEmpty()) {
				final List<FieldValue> values = new ArrayList<>();
				for (final String term : ts) {
					values.add(FieldValue.of(term));
				}

				final TermsQueryField termsQueryField = new TermsQueryField.Builder().value(values).build();

				final List<TermsQuery> shouldQueries = new ArrayList<>();

				for (final String field : SEARCH_FIELDS) {
					final TermsQuery termsQuery = new TermsQuery.Builder().field(field).terms(termsQueryField).build();

					shouldQueries.add(termsQuery);
				}

				query = new Query.Builder()
					.bool(b -> {
						shouldQueries.forEach(sq -> b.should(s -> s.terms(sq)));
						return b;
					})
					.build();
			}

			if (query == null) {
				return ResponseEntity.ok(datasetService.getPublicNotTemporaryAssets(page, pageSize));
			} else {
				return ResponseEntity.ok(datasetService.searchAssets(page, pageSize, query));
			}
		} catch (final IOException e) {
			final String error = "Unable to get datasets";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Create a new dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "201",
				description = "Dataset created.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue creating the dataset", content = @Content)
		}
	)
	public ResponseEntity<Dataset> createDataset(
		@RequestBody final Dataset dataset,
		@RequestParam(name = "project-id", required = false) final UUID projectId
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(datasetService.createAsset(dataset, projectId, permission));
		} catch (final IOException e) {
			final String error = "Unable to create dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets dataset by ID")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dataset found.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "There was no dataset found", content = @Content),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the dataset from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<Dataset> getDataset(@PathVariable("id") final UUID id) {
		final UUID projectId = datasetService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanReadOrNone(
			currentUserService.get().getId(),
			projectId
		);

		try {
			final Optional<Dataset> dataset = datasetService.getAsset(id, permission);

			if (dataset.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			// GETs not associated to a projectId cannot read private or temporary assets
			if (
				permission.equals(Schema.Permission.NONE) && (!dataset.get().getPublicAsset() || dataset.get().getTemporary())
			) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, messages.get("rebac.unauthorized-read"));
			}

			return dataset.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final Exception e) {
			final String error = "Unable to get dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes a dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Delete dataset",
				content = {
					@Content(
						mediaType = "application/json",
						schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class)
					)
				}
			),
			@ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
		}
	)
	public ResponseEntity<ResponseDeleted> deleteDataset(@PathVariable("id") final UUID id) {
		final UUID projectId = datasetService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			datasetService.deleteAsset(id, projectId, permission);
			return ResponseEntity.ok(new ResponseDeleted("Dataset", id));
		} catch (final IOException e) {
			final String error = "Unable to delete dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Update a dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dataset updated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Dataset.class)
				)
			),
			@ApiResponse(responseCode = "404", description = "Dataset could not be found", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue updating the dataset", content = @Content)
		}
	)
	ResponseEntity<Dataset> updateDataset(@PathVariable("id") final UUID id, @RequestBody final Dataset dataset) {
		final UUID projectId = datasetService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		try {
			dataset.setId(id);
			final Optional<Dataset> updated = datasetService.updateAsset(dataset, projectId, permission);
			return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
		} catch (final IOException e) {
			final String error = "Unable to update a dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}

	@GetMapping("/{id}/download-csv")
	@Secured(Roles.USER)
	@Operation(summary = "Download dataset CSV")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dataset CSV.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CsvAsset.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the dataset from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<CsvAsset> getCsv(
		@PathVariable("id") final UUID datasetId,
		@RequestParam("filename") final String filename,
		@RequestParam(name = "limit", defaultValue = "" + DEFAULT_CSV_LIMIT, required = false) final Integer limit
	) {
		final List<List<String>> csv;
		try {
			csv = datasetService.getCSVFile(filename, datasetId, limit);
			if (csv == null) {
				final String error = "Unable to get CSV";
				log.error(error);
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
			}
		} catch (final IOException e) {
			final String error = "Unable to parse CSV";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

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
			csv.size()
		);

		final CacheControl cacheControl = CacheControl.maxAge(
			config.getCacheHeadersMaxAge(),
			TimeUnit.SECONDS
		).cachePublic();
		return ResponseEntity.ok().cacheControl(cacheControl).body(csvAsset);
	}

	@GetMapping("/{id}/download-file")
	@Secured(Roles.USER)
	@Operation(summary = "Download an arbitrary dataset file")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dataset file.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CsvAsset.class)
				)
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the dataset from the data store",
				content = @Content
			)
		}
	)
	public ResponseEntity<StreamingResponseBody> getFile(
		@PathVariable("id") final UUID datasetId,
		@RequestParam("filename") final String filename
	) {
		return datasetService.getDownloadStream(datasetId, filename);
	}

	@GetMapping("/{id}/download-url")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a presigned url to download the dataset file")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Presigned url generated.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class)
				)
			),
			@ApiResponse(
				responseCode = "404",
				description = "Dataset could not be found to create a URL for",
				content = @Content
			),
			@ApiResponse(
				responseCode = "500",
				description = "There was an issue retrieving the presigned url",
				content = @Content
			)
		}
	)
	public ResponseEntity<PresignedURL> getDownloadURL(
		@PathVariable("id") final UUID id,
		@RequestParam("filename") final String filename
	) {
		final UUID projectId = datasetService.getProjectIdForAsset(id);
		final Schema.Permission permission = projectService.checkPermissionCanRead(
			currentUserService.get().getId(),
			projectId
		);

		final Optional<Dataset> dataset;
		try {
			dataset = datasetService.getAsset(id, permission);
			if (dataset.isEmpty()) {
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Dataset not found");
			}
		} catch (final Exception e) {
			final String error = "Unable to get dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		if (
			dataset.get().getEsgfId() != null &&
			!dataset.get().getEsgfId().isEmpty() &&
			dataset.get().getDatasetUrls() != null &&
			!dataset.get().getDatasetUrls().isEmpty()
		) {
			final String url = dataset
				.get()
				.getDatasetUrls()
				.stream()
				.filter(fileUrl -> fileUrl.endsWith(filename))
				.findFirst()
				.orElse(null);

			if (url == null) {
				final String error = "The file " + filename + " was not found in the dataset";
				log.error(error);
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, error);
			}

			final PresignedURL presigned = new PresignedURL().setUrl(url).setMethod("GET");
			return ResponseEntity.ok(presigned);
		} else {
			try {
				final Optional<PresignedURL> url = datasetService.getDownloadUrl(id, filename);
				return url.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
			} catch (final Exception e) {
				final String error = "Unable to get download url";
				log.error(error, e);
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
			}
		}
	}

	/**
	 * Downloads a CSV file from github given the path and owner name, then uploads
	 * it to the dataset.
	 */
	@PutMapping("/{id}/upload-csv-from-github")
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a CSV file from github to a dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Uploaded the CSV file.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the CSV", content = @Content)
		}
	)
	public ResponseEntity<ResponseStatus> uploadCsvFromGithub(
		@PathVariable("id") final UUID datasetId,
		@RequestParam("path") final String path,
		@RequestParam("repo-owner-and-name") final String repoOwnerAndName,
		@RequestParam("filename") final String filename
	) {
		final UUID projectId = datasetService.getProjectIdForAsset(datasetId);
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		log.debug("Uploading CSV file from github to dataset {}", datasetId);

		// download CSV from github
		final String csvString = githubProxy.getGithubCode(repoOwnerAndName, path).getBody();

		if (csvString == null) {
			final String error = "Unable to download csv from github";
			log.error(error);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		final HttpEntity csvEntity = new StringEntity(csvString, ContentType.APPLICATION_OCTET_STREAM);
		final String[] csvRows = csvString.split("\\R");
		final String[] headers = csvRows[0].split(",");
		return uploadCSVAndUpdateColumns(datasetId, projectId, filename, csvEntity, headers, permission);
	}

	@PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads a CSV file to a dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Uploaded the CSV file.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the CSV", content = @Content)
		}
	)
	public ResponseEntity<ProjectAsset> uploadCsv(
		@RequestParam("project-id") final UUID projectId,
		@RequestParam("name") final String name,
		@RequestParam("description") final String description,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile input
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		final List<String> filenames = new ArrayList<>();
		filenames.add(filename);
		final Dataset tempDataset = new Dataset();
		tempDataset
			.setUserId(currentUserService.get().getId())
			.setFileNames(filenames)
			.setName(name)
			.setDescription(description);
		Dataset createdDataset = null;

		try {
			createdDataset = datasetService.createAsset(tempDataset, projectId, permission);
		} catch (final IOException e) {
			final String error = "Unable to create dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		try {
			log.debug("Uploading CSV file to dataset {}", createdDataset.getId());

			final byte[] csvBytes = input.getBytes();

			final HttpEntity csvEntity = new ByteArrayEntity(csvBytes, ContentType.APPLICATION_OCTET_STREAM);
			final String csvString = new String(csvBytes);
			final String[] csvRows = csvString.split("\\R");
			final String[] headers = csvRows[0].split(",");
			for (int i = 0; i < headers.length; i++) {
				// this is very ugly but we're removing opening and closing "'s around these
				// strings.
				headers[i] = headers[i].replaceAll("^\"|\"$", "");
			}
			uploadCSVAndUpdateColumns(createdDataset.getId(), projectId, filename, csvEntity, headers, permission);
		} catch (final IOException e) {
			final String error = "Unable to upload csv dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		final AssetType assetType = AssetType.DATASET;
		final ProjectAsset projectAsset = projectAssetService.createProjectAsset(
			projectId,
			assetType,
			createdDataset,
			permission
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(projectAsset);
	}

	@PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads an arbitrary file to a dataset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Uploaded the file.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class)
				)
			),
			@ApiResponse(responseCode = "500", description = "There was an issue uploading the file", content = @Content)
		}
	)
	public ResponseEntity<ProjectAsset> uploadData(
		@RequestParam("project-id") final UUID projectId,
		@RequestParam("name") final String name,
		@RequestParam("description") final String description,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile input
	) {
		final Schema.Permission permission = projectService.checkPermissionCanWrite(
			currentUserService.get().getId(),
			projectId
		);

		final List<String> filenames = new ArrayList<>();
		filenames.add(filename);
		final Dataset dataset = new Dataset();
		dataset
			.setUserId(currentUserService.get().getId())
			.setFileNames(filenames)
			.setName(name)
			.setDescription(description);
		Dataset newDataset = null;

		try {
			newDataset = datasetService.createAsset(dataset, projectId, permission);
		} catch (final IOException e) {
			final String error = "Unable to create dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		try {
			log.debug("Uploading file to dataset {}", newDataset.getId());

			final ResponseEntity<Void> res = datasetService.getUploadStream(newDataset.getId(), filename, input);
			if (res.getStatusCode() == HttpStatus.OK) {
				// add the filename to existing file names
				Optional<Dataset> updatedDataset = datasetService.getAsset(newDataset.getId(), permission);
				if (updatedDataset.isEmpty()) {
					final String error = "Failed to get dataset after upload";
					log.error(error);
					throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
				}

				if (!updatedDataset.get().getFileNames().contains(filename)) {
					updatedDataset.get().getFileNames().add(filename);
				}

				try {
					updatedDataset = Optional.of(datasetService.extractColumnsFromFiles(updatedDataset.get()));
				} catch (final IOException e) {
					final String error = "Unable to extract columns from dataset";
					log.error(error, e);
					// This doesn't actually warrant a 500 since its just column metadata, so we'll
					// let it pass.
				}

				datasetService.updateAsset(updatedDataset.get(), projectId, permission);
			}
		} catch (final IOException e) {
			final String error = "Unable to upload file to dataset";
			log.error(error, e);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}

		final AssetType assetType = AssetType.DATASET;
		final ProjectAsset projectAsset = projectAssetService.createProjectAsset(
			projectId,
			assetType,
			newDataset,
			permission
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(projectAsset);
	}

	//	@GetMapping("/{id}/upload-url")
	//	@Secured(Roles.USER)
	//	@Operation(summary = "Gets a presigned url to upload the dataset")
	//	@ApiResponses(
	//		value = {
	//			@ApiResponse(
	//				responseCode = "200",
	//				description = "Presigned url generated.",
	//				content = @Content(
	//					mediaType = "application/json",
	//					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PresignedURL.class)
	//				)
	//			),
	//			@ApiResponse(
	//				responseCode = "500",
	//				description = "There was an issue retrieving the presigned url",
	//				content = @Content
	//			)
	//		}
	//	)
	//	public ResponseEntity<PresignedURL> getUploadURL(
	//		@PathVariable("id") final UUID id,
	//		@RequestParam("filename") final String filename
	//	) {
	//		try {
	//			return ResponseEntity.ok(datasetService.getUploadUrl(id, filename));
	//		} catch (final Exception e) {
	//			final String error = "Unable to get upload url";
	//			log.error(error, e);
	//			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
	//		}
	//	}

	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS
	 * then push the file to S3 via a
	 * presigned URL and update the dataset with the headers.
	 *
	 * <p>
	 * If the headers fail to update there will be a print to the log, however, the
	 * response will just be the status
	 * of the original csv upload.
	 *
	 * @param datasetId ID of the dataset to upload to
	 * @param filename  CSV file to upload
	 * @param csvEntity CSV file as an HttpEntity
	 * @param headers   headers of the CSV file
	 * @return Response from the upload
	 */
	private ResponseEntity<ResponseStatus> uploadCSVAndUpdateColumns(
		final UUID datasetId,
		final UUID projectId,
		final String filename,
		final HttpEntity csvEntity,
		final String[] headers,
		final Schema.Permission hasWritePermission
	) {
		try {
			// upload CSV to S3
			final Integer status = datasetService.uploadFile(datasetId, filename, csvEntity);

			// update dataset with headers if the previous upload was successful
			if (status == HttpStatus.OK.value()) {
				log.debug("Successfully uploaded CSV file to dataset {}. Now updating TDS with headers", datasetId);

				final Optional<Dataset> updatedDataset = datasetService.getAsset(datasetId, hasWritePermission);
				if (updatedDataset.isEmpty()) {
					log.error("Failed to get dataset {} after upload", datasetId);
					return ResponseEntity.internalServerError().build();
				}

				DatasetService.addDatasetColumns(updatedDataset.get(), filename, Arrays.asList(headers));

				// add the filename to existing file names
				if (!updatedDataset.get().getFileNames().contains(filename)) {
					updatedDataset.get().getFileNames().add(filename);
				}

				datasetService.updateAsset(updatedDataset.get(), projectId, hasWritePermission);
			}

			return ResponseEntity.ok(new ResponseStatus(status));
		} catch (final IOException e) {
			log.error("Unable to upload csv data", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to PUT csv data"
			);
		}
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

	@GetMapping("/{id}/preview")
	@Secured(Roles.USER)
	@Operation(summary = "Gets a preview of the data asset")
	@ApiResponses(
		value = {
			@ApiResponse(
				responseCode = "200",
				description = "Dataset preview.",
				content = @Content(
					mediaType = "application/json",
					schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = JsonNode.class)
				)
			),
			@ApiResponse(responseCode = "415", description = "Dataset cannot be previewed", content = @Content),
			@ApiResponse(responseCode = "500", description = "There was an issue generating the preview", content = @Content)
		}
	)
	public ResponseEntity<JsonNode> getPreview(
		@PathVariable("id") final UUID id,
		@RequestParam("filename") final String filename
	) {
		// Currently `climate-data` service can only work on NetCDF files it knows about
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
		// try {
		// if (filename.endsWith(".nc")) {
		// return climateDataProxy.previewEsgf(id.toString(), null, null, null);
		// } else {
		// final Optional<PresignedURL> url = datasetService.getDownloadUrl(id,
		// filename);
		// // TODO: This attempts to check the file, but fails to open the file, might
		// need
		// // to write a NetcdfFiles Stream reader
		// try (final NetcdfFile ncFile = NetcdfFiles.open(url.get().getUrl())) {
		// final ImmutableList<Attribute> globalAttributes =
		// ncFile.getGlobalAttributes();
		// for (final Attribute attribute : globalAttributes) {
		// final String name = attribute.getName();
		// final Array values = attribute.getValues();
		// // log.info("[{},{}]", name, values);
		// }
		// return climateDataProxy.previewEsgf(id.toString(), null, null, null);
		// } catch (final IOException ioe) {
		// throw new ResponseStatusException(
		// org.springframework.http.HttpStatus.valueOf(415),
		// "Unable to open file");
		// }
		// }
		// } catch (final Exception e) {
		// final String error = "Unable to get download url";
		// log.error(error, e);
		// throw new ResponseStatusException(
		// org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
		// error);
		// }
	}
}
