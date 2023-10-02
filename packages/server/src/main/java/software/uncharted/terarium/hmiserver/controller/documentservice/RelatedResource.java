package software.uncharted.terarium.hmiserver.controller.documentservice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDRelatedDocumentsResponse;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDRelatedWordsResponse;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;

@RequestMapping("/document/related")
@RestController
@Slf4j
public class RelatedResource {

	@Autowired
	DocumentProxy proxy;

	@GetMapping("/document")
	public ResponseEntity<XDDRelatedDocumentsResponse> getRelatedDocuments(
		@RequestParam("set") String set,
		@RequestParam("docid") String docid) {
		try {
			XDDRelatedDocumentsResponse response = proxy.getRelatedDocuments(set, docid);
			if (response.getData() == null || response.getData().isEmpty())
				return ResponseEntity.noContent().build();

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			log.error("There was an error finding related documents", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@RequestMapping("/word")
	public ResponseEntity<XDDRelatedWordsResponse> getRelatedWords(
		@RequestParam("set") String set,
		@RequestParam("word") String word) {
		try {
			XDDRelatedWordsResponse response = proxy.getRelatedWords(set, word);
			if (response.getData() == null || response.getData().isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			log.error("An error occurred finding related words", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
