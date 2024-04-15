package software.uncharted.terarium.hmiserver.controller.dataservice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseDeleted;
import software.uncharted.terarium.hmiserver.models.dataservice.multiphysics.DecapodesContext;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.DecapodesContextService;

@RequestMapping("/decapodes-contexts")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DecapodesContextController {

    final DecapodesContextService decapodesContextService;

    @GetMapping("/{id}")
    @Secured(Roles.USER)
    @Operation(summary = "Gets a decapodes context by ID")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Decapodes context found.",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @io.swagger.v3.oas.annotations.media.Schema(
                                                        implementation = DecapodesContext.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "There was no decapodes context found",
                        content = @Content),
                @ApiResponse(
                        responseCode = "500",
                        description = "There was an issue retrieving the decapodes context from the data store",
                        content = @Content)
            })
    ResponseEntity<DecapodesContext> getDecapodesContext(@PathVariable("id") final UUID id) {

        try {

            // Fetch the decapodes context from the data-service
            final Optional<DecapodesContext> context = decapodesContextService.getDecapodesContext(id);
            return context.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (final IOException e) {
            final String error = "Unable to get decapodes context";
            log.error(error, e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
        }
    }

    @PutMapping("/{id}")
    @Secured(Roles.USER)
    @Operation(summary = "Update a decapodes context")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Decapodes context updated.",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @io.swagger.v3.oas.annotations.media.Schema(
                                                        implementation = DecapodesContext.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "There was an issue updating the decapodes context",
                        content = @Content)
            })
    ResponseEntity<DecapodesContext> updateDecapodesContext(
            @PathVariable("id") final UUID id, @RequestBody final DecapodesContext context) {

        try {
            context.setId(id);
            final Optional<DecapodesContext> updated = decapodesContextService.updateDecapodesContext(context);
            return updated.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (final IOException e) {
            final String error = "Unable to update decapodes context";
            log.error(error, e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
        }
    }

    @DeleteMapping("/{id}")
    @Secured(Roles.USER)
    @Operation(summary = "Deletes an decapodes context")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Deleted decapodes context",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema =
                                            @io.swagger.v3.oas.annotations.media.Schema(
                                                    implementation = ResponseDeleted.class))
                        }),
                @ApiResponse(responseCode = "500", description = "An error occurred while deleting", content = @Content)
            })
    ResponseEntity<ResponseDeleted> deleteDecapodesContext(@PathVariable("id") final UUID id) {

        try {
            decapodesContextService.deleteDecapodesContext(id);
            return ResponseEntity.ok(new ResponseDeleted("DecapodesContext", id));
        } catch (final IOException e) {
            final String error = "Unable to delete decapodes context";
            log.error(error, e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
        }
    }

    @PostMapping
    @Secured(Roles.USER)
    @Operation(summary = "Create a new decapodes context")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Decapodes context created.",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema =
                                                @io.swagger.v3.oas.annotations.media.Schema(
                                                        implementation = DecapodesContext.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "There was an issue creating the decapodes context",
                        content = @Content)
            })
    ResponseEntity<DecapodesContext> createDecapodesContext(@RequestBody DecapodesContext context) {

        try {
            context = decapodesContextService.createDecapodesContext(context);
            return ResponseEntity.status(HttpStatus.CREATED).body(context);
        } catch (final IOException e) {
            final String error = "Unable to create decapodes context";
            log.error(error, e);
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
        }
    }
}
