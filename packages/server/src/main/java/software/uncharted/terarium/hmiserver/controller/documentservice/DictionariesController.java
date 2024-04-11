package software.uncharted.terarium.hmiserver.controller.documentservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDDictionariesResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DictionariesController {

  final DocumentProxy proxy;

  @GetMapping("/dictionaries")
  @Secured(Roles.USER)
  public ResponseEntity<XDDResponse<XDDDictionariesResponseOK>> getAvailableDictionaries(
      @RequestParam(name = "all", defaultValue = "") final String all) {
    try {
      final XDDResponse<XDDDictionariesResponseOK> response = proxy.getAvailableDictionaries(all);

      if (response.getErrorMessage() != null) {
        return ResponseEntity.internalServerError().build();
      }

      if (response.getSuccess() == null || response.getSuccess().getData().isEmpty())
        return ResponseEntity.noContent().build();

      return ResponseEntity.ok(response);

    } catch (final FeignException e) {
      log.error("xDD returned an exception for dictionaries search:", e);
      throw new ResponseStatusException(
          HttpStatusCode.valueOf(e.status()),
          "There was an issue with the dictionaries request to xDD");
    } catch (final Exception e) {
      log.error("Unable to find dictionaries, an error occurred", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find dictionaries, an error occurred");
    }
  }
}
