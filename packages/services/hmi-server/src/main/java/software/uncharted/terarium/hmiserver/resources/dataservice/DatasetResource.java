package software.uncharted.terarium.hmiserver.resources.dataservice;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import software.uncharted.terarium.hmiserver.models.dataservice.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.CsvAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Feature;
import software.uncharted.terarium.hmiserver.models.dataservice.Qualifier;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.*;
import java.lang.Math;

import lombok.extern.slf4j.Slf4j;

@Path("/api/datasets")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dataset REST Endpoints")
@Slf4j
public class DatasetResource {

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
	public Response getDatasets(
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
	public Response getDataset(
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
	@Path("/{id}/download/rawfile")
	public Response getCsv(
		@PathParam("id") final String id,
		@DefaultValue("true") @QueryParam("wide_format") final Boolean wideFormat,
		@DefaultValue("false") @QueryParam("data_annotation_flag") final Boolean dataAnnotationFlag,
		@DefaultValue("50") @QueryParam("row_limit") final Integer rowLimit,
		@DefaultValue("0") @QueryParam("binCount") final Integer binCount
	) {
		
		log.warn("Getting csv content");
		Response returnResponse = proxy.getCsv(id, wideFormat, dataAnnotationFlag, rowLimit);
		String rawCsvString = proxy.getCsv(id, wideFormat, dataAnnotationFlag, rowLimit).readEntity(String.class);
		List<List<String>> csv = csvToRecords(rawCsvString);
		List<String> headers = csv.get(0);
		List<List<Integer>> bins = new ArrayList<List<Integer>>();
		if (binCount > 0){ 
			log.warn("Tom");
			log.warn(csv.get(0).get(0));
			log.warn(rawCsvString);
			for (int i = 0; i < csv.get(0).size(); i++){
				log.warn("Binify column ");
				log.warn(String.valueOf(i));
				List<String> column = getColumn(csv,i);
				log.warn(column.subList(1,column.size()).toString());
				bins.add(binify(column.subList(1,column.size()), binCount)); //remove first as it is header:
			}
			log.warn("Output Headers:");
			log.warn(headers.toString());
			log.warn("Output bins:");
			log.warn(bins.toString());

		}
		CsvAsset csvAsset = new CsvAsset(csv,bins,headers);
		return Response
			.status(Response.Status.OK)
			.entity(csvAsset)
			.type(MediaType.APPLICATION_JSON)
			.build();
		
	}

	@POST
	@Path("/{id}/upload/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
		@PathParam("id") final String id,
		@QueryParam("filename") final String filename,
		final Byte[] file
	) {
		return proxy.uploadFile(id, filename, file);
	}
	
	private List<List<String>> csvToRecords(String rawCsvString){
		String[] csvRows = rawCsvString.split("\n");
		String[] headers = csvRows[0].split(",");
		List<List<String>> csv = new ArrayList<List<String>>();
		for (int i = 0; i < csvRows.length; i++){
			csv.add(Arrays.asList(csvRows[i].split(",")));
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
	private List<Integer> binify(List<String> aRow, Integer binCount){
		List<Integer> bins = new ArrayList<>();
		try {
			
			// set up row as numbers. may fail here.
			// List<Integer> numberList = aRow.stream().map(String s -> Integer.parseInt(s.trim()));
			List<Double> numberList = aRow.stream().map(Double::valueOf).collect(Collectors.toList());
			Collections.sort(numberList); 
			
			//Set up bins
			for (int i = 0; i < binCount; i++){
				bins.add(0);
			}	
			Double stepSize = (numberList.get(numberList.size() - 1) - numberList.get(0)) / (binCount - 1);
			
			// Fill bins:
			for (int i = 0; i < numberList.size(); i++){
				Integer index = (int)Math.abs(Math.floor((numberList.get(i) - numberList.get(0)) / stepSize));
				Integer value = bins.get(index);
				bins.set(index,value + 1);
			}
			// log.warn("Output bins:");
			// log.warn(bins.toString());
			return bins;
		
		}catch(RuntimeException e){
			//Cannot convert column to double, just return empty list.
			return bins;
		}
		
	}
}
