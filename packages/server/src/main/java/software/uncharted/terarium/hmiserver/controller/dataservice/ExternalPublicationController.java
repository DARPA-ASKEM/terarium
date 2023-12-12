package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.models.dataservice.ExternalPublication;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.ExternalPublicationService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * Controller class for handling external publications.
 */
@RequestMapping("/external/publications")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ExternalPublicationController{

	final ExternalPublicationService externalPublicationService;


	/**
	 * Retrieves a list of ExternalPublication objects from an Elasticsearch index.
	 *
	 * @param pageSize The number of results per page.
	 * @param page     The page number of the search results.
	 * @return A ResponseEntity containing a list of ExternalPublication objects.
	 */
	@GetMapping
	@Secured(Roles.USER)
	@Operation(summary = "Gets all external publications")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "publications found.",
			content = @Content(array = @ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExternalPublication.class)))),

		@ApiResponse(
			responseCode = "204",
			description = "There are no publications found and no errors occurred",
			content = @Content
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving publications from the data store",
			content = @Content
		)
	})
	public ResponseEntity<List<ExternalPublication>> getPublications(
		@RequestParam(name = "page_size", defaultValue = "100", required = false) final Integer pageSize,
		@RequestParam(name = "page", defaultValue = "0", required = false) final Integer page) {

		List<ExternalPublication> publications = null;
		try {
				publications = externalPublicationService.getExternalPublications(pageSize, page);
		} catch (IOException e) {
			log.error("Unable to get publications", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to get publications"
			);
		}

		if(publications.isEmpty())
			return ResponseEntity.noContent().build();

		return ResponseEntity.ok(publications);
	}

	/**
	 * Creates a new ExternalPublication by indexing it into Elasticsearch.
	 *
	 * @param publication The ExternalPublication object to be created.
	 * @return A ResponseEntity containing the id of the created ExternalPublication.
	 */
	@PostMapping()
	@Secured(Roles.USER)
	@Operation(summary = "Create a new publication")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "Publication created.",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue creating the publication",
			content = @Content
		)
	})
	public ResponseEntity<ResponseId> createPublication(
		@RequestBody final ExternalPublication publication
	) {

		ExternalPublication newExternalPublication = new ExternalPublication().setTitle(publication.getTitle()).setXddUri(publication.getXddUri());
		try {
				externalPublicationService.createExternalPublication(newExternalPublication);
		} catch (IOException e) {
			log.error("Unable to POST publication", e);
			throw new ResponseStatusException(
				org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
				"Unable to create publication"
			);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseId(newExternalPublication.getId()));
	}

	/**
	 * Retrieves an ExternalPublication object by ID.
	 *
	 * @param id The ID of the ExternalPublication to retrieve.
	 * @return The ExternalPublication object if found, or null otherwise.
	 */
	@GetMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "Gets an external publication by object id")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "publication found",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExternalPublication.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving the publication",
			content = @Content
		)
	})
	public ResponseEntity<ExternalPublication> getPublication(
		@PathVariable("id") final UUID id
	){

        try {
            return ResponseEntity.ok(externalPublicationService.getExternalPublication(id));
        } catch (IOException e) {
					log.error("Unable to GET publication", e);
					throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to get publication"
					);
        }


    }

	/**
	 * Updates a publication with the specified ID.
	 *
	 * @param id The ID of the publication to update.
	 * @param publication The updated publication data.
	 * @return A ResponseEntity with a ResponseId object containing the updated publication ID.
	 */
	@PutMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "updates an external publication by object id")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "publication updated",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseId.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue retrieving the publication",
			content = @Content
		)
	})
	public ResponseEntity<ResponseId> updatePublication(
		@PathVariable("id") final UUID id,
		@RequestBody final ExternalPublication publication
	) {
		publication.setId(id).setUpdatedOn(Timestamp.from(Instant.now()));
        try {
            externalPublicationService.updateExternalPublication(publication);
        } catch (IOException e) {
					log.error("Unable to PUT publication", e);
					throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to put publication"
					);
        }
        return ResponseEntity.ok(new ResponseId(publication.getId()));
	}

	/**
	 * Deletes a publication with the given ID.
	 *
	 * @param id The ID of the publication to delete.
	 * @return A ResponseEntity containing a ResponseDeleted object indicating the publication was successfully deleted.
	 */
	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	@Operation(summary = "deletes an external publication by object id")
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "publication deleted",
			content = @Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ResponseDeleted.class))
		),
		@ApiResponse(
			responseCode = "500",
			description = "There was an issue deleting the publication",
			content = @Content
		)
	})
	public ResponseEntity<ResponseDeleted> deletePublication(
		@PathVariable("id") final UUID id
	) {

        try {
            externalPublicationService.deleteExternalPublication(id);
        } catch (IOException e) {
					log.error("Unable to DELETE publication", e);
					throw new ResponseStatusException(
						org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
						"Unable to delete publication"
					);
        }
        return ResponseEntity.ok(new ResponseDeleted(ResourceType.PUBLICATION.toString(), id.toString()));
	}


}
