package software.uncharted.terarium.hmiserver.controller.documentservice;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDRelatedDocumentsResponse;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDRelatedWordsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/document/related")
@RestController
@Slf4j
@RequiredArgsConstructor
public class RelatedController {

  final DocumentProxy proxy;

  @GetMapping("/document")
  @Secured(Roles.USER)
  public ResponseEntity<XDDRelatedDocumentsResponse> getRelatedDocuments(
      @RequestParam("set") final String set, @RequestParam("docid") final String docid) {
    try {
      final XDDRelatedDocumentsResponse response = proxy.getRelatedDocuments(set, docid);
      if (response.getData() == null || response.getData().isEmpty())
        return ResponseEntity.noContent().build();

      return ResponseEntity.ok(response);

    } catch (final FeignException e) {
      log.error("xDD returned an exception for related document search:", e);
      throw new ResponseStatusException(
          HttpStatusCode.valueOf(e.status()), "There was an issue with the related request to xDD");
    } catch (final Exception e) {
      log.error("Unable to find related documents, an error occurred", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find related documents, an error occurred");
    }
  }

  @GetMapping("/word")
  @Secured(Roles.USER)
  public ResponseEntity<XDDRelatedWordsResponse> getRelatedWords(
      @RequestParam("set") final String set, @RequestParam("word") final String word) {
    try {
      final XDDRelatedWordsResponse response = proxy.getRelatedWords(set, word);
      if (response.getData() == null || response.getData().isEmpty()) {
        return ResponseEntity.noContent().build();
      }

      return ResponseEntity.ok(response);

    } catch (final FeignException e) {
      log.error("xDD returned an exception for related word search:", e);
      throw new ResponseStatusException(
          HttpStatusCode.valueOf(e.status()),
          "There was an issue with the related word request to xDD");
    } catch (final Exception e) {
      log.error("Unable to find related words, an error occurred", e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to find related words, an error occurred");
    }
  }
}
