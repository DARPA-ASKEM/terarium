package software.uncharted.terarium.hmiserver.resources.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.proxies.jsdelivr.JsDelivrProxy;
import software.uncharted.terarium.hmiserver.resources.DataStorageResource;
import software.uncharted.terarium.hmiserver.resources.SnakeCaseResource;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api/datasets")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dataset REST Endpoints")
@Slf4j
public class DatasetResource extends DataStorageResource implements SnakeCaseResource {
	private static final MediaType MEDIA_TYPE_CSV = new MediaType("text", "csv", "UTF-8");
	private static final int DEFAULT_CSV_LIMIT = 100;

	@ConfigProperty(name = "aws.bucket")
	Optional<String> bucket;

	@ConfigProperty(name = "aws.data_set_path")
	Optional<String> dataSetPath;

	@ConfigProperty(name = "aws.simulate_path")
	Optional<String> simulatePath;

	@ConfigProperty(name = "aws.access_key_id")
	Optional<String> accessKeyId;

	@ConfigProperty(name = "aws.secret_access_key")
	Optional<String> secretAccessKey;

	@ConfigProperty(name = "storage.host")
	Optional<String> storageHost;

	@ConfigProperty(name = "aws.region")
	Optional<String> region;

	@Inject
	@RestClient
	DatasetProxy datasetProxy;

	@Inject
	@RestClient
	JsDelivrProxy githubProxy;


	@GET
	public List<Dataset> getDatasets(
		@DefaultValue("500") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return datasetProxy.getDatasets(pageSize, page);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDataset(
		final Dataset dataset
	) {
		JsonNode node = convertObjectToSnakeCaseJsonNode(dataset);
		return datasetProxy.createDatasets(node);
	}

	@GET
	@Path("/{id}")
	public Dataset getDataset(
		@PathParam("id") final String id
	) {
		return datasetProxy.getDataset(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataset(
		@PathParam("id") final String id
	) {
		return datasetProxy.deleteDataset(id);
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataset(
		@PathParam("id") final String id,
		final Dataset dataset
	) {
		return datasetProxy.updateDataset(id, convertObjectToSnakeCaseJsonNode(dataset));
	}

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deprecateDataset(
		@PathParam("id") final String id
	) {
		return datasetProxy.deprecateDataset(id);
	}

	@GET
	@Path("/{datasetId}/downloadCSV")
	public Response getCsv(
		@PathParam("datasetId") final String datasetId,
		@QueryParam("filename") final String filename,
		@QueryParam(value = "limit") final Integer limit    // -1 means no limit
	) throws IOException {

		log.debug("Getting CSV content");

		//verify that dataSetPath and bucket are set. If not, return an error
		if (!dataSetPath.isPresent() || !bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot download CSV.");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		AwsCredentialsProvider awsCredentials = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));

		String objectKey = String.format("%s/%s/%s", dataSetPath.get(), datasetId, filename);

		S3Client client = S3Client.builder().region(Region.of(region.get())).credentialsProvider(awsCredentials).build();

		GetObjectRequest request = GetObjectRequest.builder()
			.bucket(bucket.get()).key(objectKey).build();

		ResponseInputStream<GetObjectResponse> s3objectResponse = client
			.getObject(request);

		final BufferedReader reader = new BufferedReader(new InputStreamReader(s3objectResponse));

		// Read the specified amount of lines, or the default (including the header)
		String line;
		final StringBuilder csvStringBuilder = new StringBuilder();
		long lineCount = 0;
		final long linesToRead = limit != null ? limit : DEFAULT_CSV_LIMIT;
		while ((line = reader.readLine()) != null) {
			csvStringBuilder.append(line).append(System.getProperty("line.separator"));
			lineCount++;
			if (linesToRead != -1 && lineCount >= linesToRead) {
				break;
			}
		}

		List<List<String>> csv = csvToRecords(csvStringBuilder.toString());
		List<String> headers = csv.get(0);
		List<CsvColumnStats> CsvColumnStats = new ArrayList<>();
		for (int i = 0; i < csv.get(0).size(); i++) {
			List<String> column = getColumn(csv, i);
			CsvColumnStats.add(getStats(column.subList(1, column.size()))); //remove first as it is header:
		}
		CsvAsset csvAsset = new CsvAsset(csv, CsvColumnStats, headers);
		return Response
			.status(Response.Status.OK)
			.entity(csvAsset)
			.type(MediaType.APPLICATION_JSON)
			.build();
	}

	/**
	 * Downloads a CSV file from github given the path and owner name, then uploads it to the dataset.
	 */
	@PUT
	@Path("/{datasetId}/uploadCSVFromGithub")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response uploadCsvFromGithub(
		@PathParam("datasetId") final String datasetId,
		@QueryParam("path") final String path,
		@QueryParam("repoOwnerAndName") final String repoOwnerAndName,
		@QueryParam("filename") final String filename
	) {
		log.debug("Uploading CSV file from github to dataset {}", datasetId);

		//verify that dataSetPath and bucket are set. If not, return an error
		if (!dataSetPath.isPresent()) {
			log.error("dataSetPath information not set. Cannot upload CSV from github.");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		//download CSV from github
		String csvString = githubProxy.getGithubCode(repoOwnerAndName, path);


		String objectKey = String.format("%s/%s/%s", dataSetPath.get(), datasetId, filename);
		SdkHttpResponse res = uploadStringToS3(objectKey, csvString);

		//find the status of the response
		if (res.isSuccessful()) {
			log.debug("Successfully uploaded CSV file to dataset {}", datasetId);
			return Response
				.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON)
				.build();
		} else {
			log.error("Failed to upload CSV file to dataset {}", datasetId);
			return Response.status(res.statusCode(), res.statusText().get()).type(MediaType.APPLICATION_JSON)
				.build();
		}
	}


	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS then push
	 * the file to S3.
	 *
	 * @param datasetId ID of the dataset to upload to
	 * @param filename  CSV file to upload
	 * @return
	 */
	@PUT
	@Path("/{datasetId}/uploadCSV")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadCsv(
		@PathParam("datasetId") final String datasetId,
		@QueryParam("filename") final String filename,
		Map<String, InputStream> input
	) throws IOException {

		log.debug("Uploading CSV file to dataset {}", datasetId);

		//verify that dataSetPath and bucket are set. If not, return an error
		if (!dataSetPath.isPresent()) {
			log.error("dataset path information not set. Cannot upload CSV.");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		String objectKey = String.format("%s/%s/%s", dataSetPath.get(), datasetId, filename);

		SdkHttpResponse res = uploadBytesToS3(objectKey, input.get("file").readAllBytes());

		//find the status of the response
		if (res.isSuccessful()) {
			log.debug("Successfully uploaded CSV file to dataset {}", datasetId);
			return Response
				.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON)
				.build();
		} else {
			log.error("Failed to upload CSV file to dataset {}", datasetId);
			return Response.status(res.statusCode(), res.statusText().get()).type(MediaType.APPLICATION_JSON)
				.build();
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
		for (int i = 0; i < matrix.size(); i++) {
			if (matrix.get(i).size() > columnNumber) {
				column.add(matrix.get(i).get(columnNumber));
			}

		}
		return column;
	}

	/**
	 * Given a column and an amount of bins create a CsvColumnStats object.
	 *
	 * @param aCol
	 * @return
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

	/**
	 * Get a signed url for uploading a file.
	 *
	 * @param id
	 * @param fileName
	 * @return signed URL
	 */
	@GET
	@Path("/{id}/upload-url")
	@Tag(description = "Get a signed url for uploading a file")
	public PresignedURL getUploadUrl(@PathParam("id") final String id, @QueryParam("filename") final String fileName) {
		return datasetProxy.getUploadUrl(id, fileName);
	}

	/**
	 * Get a download url for a file.
	 *
	 * @param id
	 * @param filename
	 * @return signed URL
	 */
	@GET
	@Path("/{id}/download-url")
	@Tag(description = "Get a download url for a file")
	public PresignedURL getDownloadUrl(@PathParam("id") final String id, @QueryParam("filename") String filename) {
		return datasetProxy.getDownloadUrl(id, filename);
	}
}
