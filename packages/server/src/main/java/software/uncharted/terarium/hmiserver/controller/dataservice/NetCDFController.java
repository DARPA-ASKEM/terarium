package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.NetCDF;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.NetCDFService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/netcdfs")
@RestController
@Slf4j
@RequiredArgsConstructor
public class NetCDFController {
	final NetCDFService netCDFService;

	public ResponseEntity<List<NetCDF>> getNetCDFs(
		@RequestParam(name = "page-size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {
		try {
			return ResponseEntity.ok(netCDFService.getNetCDFs(page, pageSize));
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
			return ResponseEntity.status(HttpStatus.CREATED).body(netCDFService.createNetCDF(netCDF));
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
			Optional<NetCDF> netCDF = netCDFService.getNetCDF(netCDFId);
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
		@ApiResponse(responseCode = "204", description = "NetCDF not found", content = @Content),
		@ApiResponse(responseCode = "500", description = "There was an issue updating the netCDF", content = @Content)
	})
	public ResponseEntity<NetCDF> updateNetCDF(
		@PathVariable("id") UUID netCDFId,
		@RequestBody NetCDF netCDF) {

		try {
			netCDF.setId(netCDFId);
			Optional<NetCDF> updated = netCDFService.updateNetCDF(netCDF);
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
			netCDFService.deleteNetCDF(netCDFId);
			return ResponseEntity.ok(new ResponseDeleted("netCDF", netCDFId));
		} catch (Exception e) {
			final String error = "Unable to delete netCDF";
			log.error(error, e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				error);
		}
	}
}
