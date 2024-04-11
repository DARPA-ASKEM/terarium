package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.util.List;
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
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.data.EquationService;

/**
 * Rest controller for storing, retrieving, modifying and deleting equation assets in the
 * dataservice
 */
@RequestMapping("/equations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EquationController {

  final EquationService equationService;
  final ObjectMapper objectMapper;

  /**
   * Retrieve the list of Equations
   *
   * @param pageSize number of equations per page
   * @param page current page number
   * @return list of equations
   */
  @GetMapping
  @Secured(Roles.USER)
  @Operation(summary = "Gets all equations")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equations found.",
            content =
                @Content(
                    array =
                        @ArraySchema(
                            schema =
                                @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = Equation.class)))),
        @ApiResponse(
            responseCode = "500",
            description = "There was an issue retrieving equations from the data store",
            content = @Content)
      })
  ResponseEntity<List<Equation>> getEquations(
      @RequestParam(name = "page-size", defaultValue = "100") final Integer pageSize,
      @RequestParam(name = "page", defaultValue = "0") final Integer page) {

    try {
      return ResponseEntity.ok(equationService.getAssets(pageSize, page));
    } catch (final IOException e) {
      final String error = "Unable to get equations";
      log.error(error, e);
      throw new ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
  }

  /**
   * Create equation and return its ID
   *
   * @param equation equation to create
   * @return new ID for equation
   */
  @PostMapping
  @Secured(Roles.USER)
  @Operation(summary = "Create a new equation")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Equation created.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = Equation.class))),
        @ApiResponse(
            responseCode = "500",
            description = "There was an issue creating the equation",
            content = @Content)
      })
  ResponseEntity<Equation> createEquation(@RequestBody Equation equation) {

    try {
      equation = equationService.createAsset(equation);
      return ResponseEntity.status(HttpStatus.CREATED).body(equation);
    } catch (final IOException e) {
      final String error = "Unable to create equation";
      log.error(error, e);
      throw new ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
  }

  /**
   * Retrieve an equation
   *
   * @param id equation id
   * @return Equation
   */
  @GetMapping("/{id}")
  @Secured(Roles.USER)
  @Operation(summary = "Gets equation by ID")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equation found.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = Equation.class))),
        @ApiResponse(
            responseCode = "404",
            description = "There was no equation found",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "There was an issue retrieving the equation from the data store",
            content = @Content)
      })
  ResponseEntity<Equation> getEquation(@PathVariable("id") final UUID id) {

    try {
      final Optional<Equation> equation = equationService.getAsset(id);
      return equation.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    } catch (final IOException e) {
      final String error = "Unable to get equation";
      log.error(error, e);
      throw new ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
  }

  /**
   * Update an equation
   *
   * @param id id of equation to update
   * @param equation equation to update with
   * @return ID of updated equation
   */
  @PutMapping("/{id}")
  @Secured(Roles.USER)
  @Operation(summary = "Update a equation")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Equation updated.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @io.swagger.v3.oas.annotations.media.Schema(
                            implementation = Equation.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Equation could not be found",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "There was an issue updating the equation",
            content = @Content)
      })
  ResponseEntity<Equation> updateEquation(
      @PathVariable("id") final UUID id, @RequestBody final Equation equation) {

    try {
      equation.setId(id);
      final Optional<Equation> updated = equationService.updateAsset(equation);
      return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    } catch (final IOException e) {
      final String error = "Unable to update equation";
      log.error(error, e);
      throw new ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
  }

  /**
   * Deletes and equation
   *
   * @param id equation to delete
   * @return delete message
   */
  @DeleteMapping("/{id}")
  @Secured(Roles.USER)
  @Operation(summary = "Deletes an equation")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Deleted equation",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema =
                      @io.swagger.v3.oas.annotations.media.Schema(
                          implementation = ResponseDeleted.class))
            }),
        @ApiResponse(
            responseCode = "500",
            description = "An error occurred while deleting",
            content = @Content)
      })
  ResponseEntity<ResponseDeleted> deleteEquation(@PathVariable("id") final UUID id) {

    try {
      equationService.deleteAsset(id);
      return ResponseEntity.ok(new ResponseDeleted("Equation", id));
    } catch (final IOException e) {
      final String error = "Unable to delete equation";
      log.error(error, e);
      throw new ResponseStatusException(
          org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, error);
    }
  }
}
