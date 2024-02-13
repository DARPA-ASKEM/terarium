package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.models.dataservice.NetCDF;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseStatus;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.NetCDFService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/netcdfs")
@RestController
@Slf4j
@RequiredArgsConstructor
public class NetCDFController {
	final NetCDFService netCDFService;

	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets a list of netCDFs")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "NetCDFs retrieved.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NetCDF.class))),
		@ApiResponse(responseCode = "500", description = "There was an issue retrieving the netCDFs", content = @Content)
	})
	public ResponseEntity<List<NetCDF>> getNetCDFs(
		@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {
		try {
			return ResponseEntity.ok(netCDFService.getAssets(page, pageSize));
		} catch (Exception e) {
			final String error = "Unable to get netCDFs";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	@PostMapping
	@Secured(Roles.USER)
	@Operation(summary = "Creates a new netCDF")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "NetCDF created.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NetCDF.class))),
		@ApiResponse(responseCode = "500", description = "There was an issue creating the netCDF", content = @Content)
	})
	public ResponseEntity<NetCDF> createNetCDF(@RequestBody NetCDF netCDF) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(netCDFService.createAsset(netCDF));
		} catch (Exception e) {
			final String error = "Unable to create netCDF";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets an netCDF by ID")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "NetCDF retrieved.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NetCDF.class))),
		@ApiResponse(responseCode = "204", description = "NetCDF not found", content = @Content),
		@ApiResponse(responseCode = "500", description = "There was an issue retrieving the netCDF", content = @Content)
	})
	public ResponseEntity<NetCDF> getNetCDF(@PathVariable("id") UUID netCDFId) {
		try {
			Optional<NetCDF> netCDF = netCDFService.getAsset(netCDFId);
			if (netCDF.isEmpty()) {
				return ResponseEntity.noContent().build();
			}
			return ResponseEntity.ok(netCDF.get());
		} catch (Exception e) {
			final String error = "Unable to get netCDF";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Updates an netCDF")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "NetCDF updated.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = NetCDF.class))),
		@ApiResponse(responseCode = "404", description = "NetCDF not found", content = @Content),
		@ApiResponse(responseCode = "500", description = "There was an issue updating the netCDF", content = @Content)
	})
	public ResponseEntity<NetCDF> updateNetCDF(
		@PathVariable("id") UUID netCDFId,
		@RequestBody NetCDF netCDF) {

		try {
			netCDF.setId(netCDFId);
			Optional<NetCDF> updated = netCDFService.updateAsset(netCDF);
			if (updated.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(updated.get());
		} catch (Exception e) {
			final String error = "Unable to update netCDF";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Deletes an netCDF")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "NetCDF deleted.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class))),
		@ApiResponse(responseCode = "500", description = "There was an issue deleting the netCDF", content = @Content)
	})
	public ResponseEntity<ResponseDeleted> deleteNetCDF(@PathVariable("id") UUID netCDFId) {

		try {
			netCDFService.deleteAsset(netCDFId);
			return ResponseEntity.ok(new ResponseDeleted("netCDF", netCDFId));
		} catch (Exception e) {
			final String error = "Unable to delete netCDF";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	/**
	 * Uploads a file to the project.
	 */
	@PutMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Secured(Roles.USER)
	@Operation(summary = "Uploads an .nc file")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Uploaded the file.", content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseStatus.class))),
		@ApiResponse(responseCode = "404", description = "NetCDF could not be found", content = @Content),
		@ApiResponse(responseCode = "500", description = "There was an issue uploading the file", content = @Content)
	})
	public ResponseEntity<Void> uploadNCFile(
		@PathVariable("id") final UUID netCDFId,
		@RequestParam("filename") final String filename,
		@RequestPart("file") final MultipartFile file
	) {
		try {
			final Optional<NetCDF> netCDF = netCDFService.getAsset(netCDFId);
			if (netCDF.isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			final byte[] fileAsBytes = file.getBytes();
			final HttpEntity fileEntity = new ByteArrayEntity(fileAsBytes, ContentType.APPLICATION_OCTET_STREAM);
			return uploadNCFileHelper(netCDF.get(), filename, fileEntity);
		} catch (final IOException e) {
			final String error = "Unable to upload file";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}

	/**
	 * Uploads an nc file inside the entity to TDS via a presigned URL
	 *
	 * @param netCDF		 The netCDF associated with the nc file
	 * @param fileName   The name of the file to upload
	 * @param fileEntity The entity containing the file to upload
	 * @return A response containing the status of the upload
	 */
	private ResponseEntity<Void> uploadNCFileHelper(final NetCDF netCDF, final String fileName,
																										final HttpEntity fileEntity) {
		try (final CloseableHttpClient httpclient = HttpClients.custom()
			.disableRedirectHandling()
			.build()) {

			// upload file to S3
			final PresignedURL presignedURL = netCDFService.getUploadUrl(netCDF.getId(), fileName);
			final HttpPut put = new HttpPut(presignedURL.getUrl());
			put.setEntity(fileEntity);
			final HttpResponse response = httpclient.execute(put);

			if (netCDF.getFileNames() == null) {
				netCDF.setFileNames(new ArrayList<>());
			}
			netCDF.getFileNames().add(fileName);
			netCDFService.updateAsset(netCDF);
			// TODO: decode NC file
//			NetCDF updatedNetCDF = netCDFService.decodeNCFile(netCDF, fileEntity.getContent());
//			netCDFService.updateNetCDF(updatedNetCDF);

			return ResponseEntity.status(response.getStatusLine().getStatusCode()).build();
		} catch (final IOException e) {
			final String error = "Unable to upload document";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
		}
	}
}
