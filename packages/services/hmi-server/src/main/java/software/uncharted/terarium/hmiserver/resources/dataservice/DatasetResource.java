package software.uncharted.terarium.hmiserver.resources.dataservice;

import org.apache.james.mime4j.dom.field.FieldName;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.plugins.providers.multipart.*;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvColumnStats;
import software.uncharted.terarium.hmiserver.models.dataservice.Feature;
import software.uncharted.terarium.hmiserver.models.dataservice.Qualifier;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.PresignedURL;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
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
		return proxy.createFeatures(feature);
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
		return proxy.updateFeature(id, feature);
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
		return proxy.createQualifiers(qualifier);
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
		return proxy.updateQualifier(id, qualifier);
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
	public Response createDatasets(
		final Dataset dataset
	) {
		return proxy.createDatasets(dataset);
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
		return proxy.updateDataset(id, dataset);
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
		@PathParam("datasetId") final String id,
		@QueryParam("filename") final String filename
	) {

		log.debug("Getting CSV content");
		PresignedURL downloadUrl;

		try {
			downloadUrl = proxy.getDownloadUrl(id, filename);
		} catch (RuntimeException e) {
			log.error("Unable to get CSV", e);
			return Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.build();
		}

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(downloadUrl.getUrl()))
			.GET()
			.build();

		HttpClient client = HttpClient.newHttpClient();
		AtomicReference<String> rawCsvString = new AtomicReference<>();
		client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenAccept(body -> rawCsvString.set(body))
			.join();



		List<List<String>> csv = csvToRecords(rawCsvString.get());
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
	 * Converting a multipart form input to an output to pass along to a proxied endpoint.
	 * @param input non null input
	 * @param fileName non null file name
	 * @return a multi part form data output object, which could be empty if we ran into any issues
	 */
	private MultipartFormDataOutput fromFormDataInputToFormDataOutput(MultipartFormDataInput input, String fileName) {
		MultipartFormDataOutput mdo = new MultipartFormDataOutput();
		int i = 0;
		for (Map.Entry < String, List < InputPart >> inputPartEntry: input.getFormDataMap().entrySet()) {
			String partId = inputPartEntry.getKey();
			List < InputPart > inputParts = inputPartEntry.getValue();

			for (InputPart part: inputParts) {
				InputStream inputStream;
				try {
					inputStream = part.getBody(InputStream.class, null);
					OutputPart objPart = mdo.addFormData(partId , inputStream, part.getMediaType());
					objPart.getHeaders().putSingle(FieldName.CONTENT_DISPOSITION, "form-data; name=" + partId + "; filename=" + fileName);
				} catch (IOException e) {
					log.error("Error converting to MultipartFormDataInput", e);
				}
			}
		}

		return mdo;
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
		   column.add(i,maxtrix.get(i).get(columnNumber));
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
			int binCount = 50;
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
}
