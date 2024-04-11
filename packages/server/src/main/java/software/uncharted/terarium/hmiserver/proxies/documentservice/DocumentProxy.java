package software.uncharted.terarium.hmiserver.proxies.documentservice;

import javax.ws.rs.DefaultValue;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.*;

@FeignClient(name = "documentService", url = "${xdd-prod-service.url}")
public interface DocumentProxy {
  @GetMapping("/api/v2/articles")
  XDDResponse<DocumentsResponseOK> getDocuments(
      @RequestParam("api_key") String apiKey,
      @RequestParam(required = false, name = "docid") String docid,
      @RequestParam(required = false, name = "doi") String doi,
      @RequestParam(required = false, name = "title") String title,
      @RequestParam(required = false, name = "term") String term,
      @RequestParam(required = false, name = "dataset") String dataset,
      @RequestParam(required = false, name = "include_score") String include_score,
      @RequestParam(required = false, name = "include_highlights") String include_highlights,
      @RequestParam(required = false, name = "inclusive") String inclusive,
      @RequestParam(required = false, name = "full_results") String full_results,
      @RequestParam(required = false, name = "max") String max,
      @RequestParam(required = false, name = "per_page") String per_page,
      @RequestParam(required = false, name = "dict") String dict,
      @RequestParam(required = false, name = "facets") String facets,
      @RequestParam(required = false, name = "min_published") String min_published,
      @RequestParam(required = false, name = "max_published") String max_published,
      @RequestParam(required = false, name = "pubname") String pubname,
      @RequestParam(required = false, name = "publisher") String publisher,
      @RequestParam(required = false, name = "additional_fields") String additional_fields,
      @RequestParam(required = false, name = "match") String match,
      @RequestParam(required = false, name = "known_entities") String known_entities,
      @RequestParam(required = false, name = "github_url") String github_url,
      @RequestParam(required = false, name = "similar_to") String similar_to,
      @RequestParam(required = false, name = "askem_object_limit", defaultValue = "5")
          String askem_object_limit);

  @GetMapping("/sets")
  XDDSetsResponse getAvailableSets();

  @GetMapping("/api/v2/dictionaries")
  XDDResponse<XDDDictionariesResponseOK> getAvailableDictionaries(
      @RequestParam("all") @DefaultValue("") String all);

  @GetMapping("/sets/{set}/doc2vec/api/similar")
  XDDRelatedDocumentsResponse getRelatedDocuments(
      @PathVariable("set") String set, @RequestParam("docid") String docid);

  @GetMapping("/sets/{set}/word2vec/api/most_similar")
  XDDRelatedWordsResponse getRelatedWords(
      @PathVariable("set") String set, @RequestParam("word") String word);
}
