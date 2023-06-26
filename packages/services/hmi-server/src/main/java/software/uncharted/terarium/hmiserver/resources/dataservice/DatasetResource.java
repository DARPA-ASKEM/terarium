package software.uncharted.terarium.hmiserver.resources.dataservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.Feature;
import software.uncharted.terarium.hmiserver.models.dataservice.Qualifier;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.lang.Math;
import com.google.common.math.Stats;
import com.google.common.math.Quantiles;


import lombok.extern.slf4j.Slf4j;

@Path("/api/datasets")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dataset REST Endpoints")
@Slf4j
public class DatasetResource {
	private static final MediaType MEDIA_TYPE_CSV = new MediaType("text","csv", "UTF-8");

	@ConfigProperty(name = "aws.bucket")
	Optional<String> bucket;

	@ConfigProperty(name = "aws.data_set_path")
	Optional<String> dataSetPath;

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
	DatasetProxy proxy;

	@GET
	@Path("/features")
	public Response getFeatures(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getFeatures(pageSize, page);
	}

	@POST
	@Path("/features")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createFeatures(
		final Feature feature
	) {
		return proxy.createFeatures(convertObjectToSnakeCaseJsonNode(feature));
	}

	@GET
	@Path("/features/{id}")
	public Response getFeature(
		@PathParam("id") final String id
	) {
		return proxy.getFeature(id);
	}

	@DELETE
	@Path("/features/{id}")
	public Response deleteFeature(
		@PathParam("id") final String id
	) {
		return proxy.deleteFeature(id);
	}

	@PATCH
	@Path("/features/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateFeature(
		@PathParam("id") final String id,
		final Feature feature
	) {
		return proxy.updateFeature(id, convertObjectToSnakeCaseJsonNode(feature));
	}

	@GET
	@Path("/qualifiers")
	public Response getQualifiers(
		@DefaultValue("100") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getQualifiers(pageSize, page);
	}

	@POST
	@Path("/qualifiers")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createQualifiers(
		final Qualifier qualifier
	) {
		return proxy.createQualifiers(convertObjectToSnakeCaseJsonNode(qualifier));
	}

	@GET
	@Path("/qualifiers/{id}")
	public Response getQualifier(
		@PathParam("id") final String id
	) {
		return proxy.getQualifier(id);
	}

	@DELETE
	@Path("/qualifiers/{id}")
	public Response deleteQualifier(
		@PathParam("id") final String id
	) {
		return proxy.deleteQualifier(id);
	}

	@PATCH
	@Path("/qualifiers/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateQualifier(
		@PathParam("id") final String id,
		final Qualifier qualifier
	) {
		return proxy.updateQualifier(id, convertObjectToSnakeCaseJsonNode(qualifier));
	}

	@GET
	public List<Dataset> getDatasets(
		@DefaultValue("500") @QueryParam("page_size") final Integer pageSize,
		@DefaultValue("0") @QueryParam("page") final Integer page
	) {
		return proxy.getDatasets(pageSize, page);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createDataset(
		final Dataset dataset
	) {
		JsonNode node = convertObjectToSnakeCaseJsonNode(dataset);
		return proxy.createDatasets(node);
	}

	@GET
	@Path("/{id}")
	public Dataset getDataset(
		@PathParam("id") final String id
	) {
		return proxy.getDataset(id);
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataset(
		@PathParam("id") final String id
	) {
		return proxy.deleteDataset(id);
	}

	@PATCH
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDataset(
		@PathParam("id") final String id,
		final Dataset dataset
	) {
		return proxy.updateDataset(id, convertObjectToSnakeCaseJsonNode(dataset));
	}

	@POST
	@Path("/deprecate/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deprecateDataset(
		@PathParam("id") final String id
	) {
		return proxy.deprecateDataset(id);
	}

	@GET
	@Path("/{datasetId}/downloadCSV")
	public Response getCsv(
		@PathParam("datasetId") final String datasetId,
		@QueryParam("filename") final String filename
	) {

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

		String csvString = new BufferedReader(new InputStreamReader(s3objectResponse))
			.lines()
			.collect(Collectors.joining("\n"));

		List<List<String>> csv = csvToRecords(csvString);
		List<String> headers = csv.get(0);
		List<CsvColumnStats> CsvColumnStats = new ArrayList<>();
		for (int i = 0; i < csv.get(0).size(); i++){
			List<String> column = getColumn(csv,i);
			CsvColumnStats.add(getStats(column.subList(1,column.size()))); //remove first as it is header:
		}
		CsvAsset csvAsset = new CsvAsset(csv,CsvColumnStats,headers);
		return Response
			.status(Response.Status.OK)
			.entity(csvAsset)
			.type(MediaType.APPLICATION_JSON)
			.build();
	}

	/**
	 * Uploads a CSV file to the dataset. This will grab a presigned URL from TDS then push
	 * the file to S3.
	 * @param datasetId ID of the dataset to upload to
	 * @param filename CSV file to upload
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
		if (!dataSetPath.isPresent() || !bucket.isPresent() || !accessKeyId.isPresent() || !secretAccessKey.isPresent()) {
			log.error("S3 information not set. Cannot upload CSV.");
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		String objectKey = String.format("%s/%s/%s", dataSetPath.get(), datasetId, filename);


		//init our S3 client
		AwsCredentialsProvider awsCredentials = StaticCredentialsProvider.create(
			AwsBasicCredentials.create(accessKeyId.get(), secretAccessKey.get()));
		S3Client client = S3Client.builder().region(Region.of(region.get())).credentialsProvider(awsCredentials).build();

		PutObjectRequest request = PutObjectRequest.builder().bucket(bucket.get()).key(objectKey).build();

		PutObjectResponse res = client.putObject(request, RequestBody.fromBytes(input.get("file").readAllBytes()));

		//find the status of the response
		if (res.sdkHttpResponse().isSuccessful()) {
			log.debug("Successfully uploaded CSV file to dataset {}", datasetId);
			return Response
				.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON)
				.build();
		} else {
			log.error("Failed to upload CSV file to dataset {}", datasetId);
			return Response.status(res.sdkHttpResponse().statusCode(), res.sdkHttpResponse().statusText().get()).type(MediaType.APPLICATION_JSON)
				.build();
		}


	}


	// TODO: https://github.com/DARPA-ASKEM/Terarium/issues/1005
	// warning this is not sufficient for the long term. Should likely use a library for this conversion formatting may break this.
	private List<List<String>> csvToRecords(String rawCsvString){
		String[] csvRows = rawCsvString.split("\n");
		String[] headers = csvRows[0].split(",");
		List<List<String>> csv = new ArrayList<>();
		for (String csvRow : csvRows) {
			csv.add(Arrays.asList(csvRow.split(",")));
		}
		return csv;
	}

	private List<String> getColumn(List<List<String>> maxtrix, int columnNumber){
		List<String> column = new ArrayList<>();
		for(int i = 0 ; i < maxtrix.size(); i++){
			if(maxtrix.get(i).size() > columnNumber){
				column.add(i,maxtrix.get(i).get(columnNumber));
			}

		}
		return column;
	}

	/**
	 * Given a column and an amount of bins create a CsvColumnStats object.
	 * @param aCol
	 * @return
	 */
	private CsvColumnStats getStats(List<String> aCol){
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
			for (int i = 0; i < binCount; i++){
				bins.add(0);
			}
			double stepSize = (numberList.get(numberList.size() - 1) - numberList.get(0)) / (binCount - 1);

			// Fill bins:
			for (Double aDouble : numberList) {
				int index = (int) Math.abs(Math.floor((aDouble - numberList.get(0)) / stepSize));
				Integer value = bins.get(index);
				bins.set(index, value + 1);
			}

			return new CsvColumnStats(bins,minValue,maxValue,meanValue,medianValue,sdValue);

		}catch(Exception e){
			//Cannot convert column to double, just return empty list.
			return new CsvColumnStats(bins,0,0,0,0,0);
		}
	}

	/**
	 * Get a signed url for uploading a file.
	 * @param id
	 * @param fileName
	 * @return signed URL
	 */
	@GET
	@Path("/{id}/upload-url")
	@Tag(description = "Get a signed url for uploading a file")
	public PresignedURL getUploadUrl(@PathParam("id") final String id, @QueryParam("filename") final String fileName){
		return proxy.getUploadUrl(id, fileName);
	}

	/**
	 * Get a download url for a file.
	 * @param id
	 * @param filename
	 * @return signed URL
	 */
	@GET
	@Path("/{id}/download-url")
	@Tag(description = "Get a download url for a file")
	public PresignedURL getDownloadUrl(@PathParam("id") final String id,  @QueryParam("filename") String filename) {
		return proxy.getDownloadUrl(id, filename);
	}

	/**
	 * Serialize a given object to be in snake-case instead of camelCase, as may be
	 * required by the proxied endpoint.
	 * @param object
	 * @return
	 */
	private JsonNode convertObjectToSnakeCaseJsonNode(Object object) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		return mapper.convertValue(object, JsonNode.class);
	}
}
