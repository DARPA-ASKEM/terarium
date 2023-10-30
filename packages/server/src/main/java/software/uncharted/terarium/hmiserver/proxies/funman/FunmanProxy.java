package software.uncharted.terarium.hmiserver.proxies.funman;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.funman.FunmanPostQueriesRequest;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;

@FeignClient(name = "funman-api", url = "${funman-service.url}", path="/queries")
public interface FunmanProxy {

    @GetMapping("/{query_id}/halt")
    ResponseEntity<JsonNode> halt(@PathVariable("query_id") String queryId);
   
    @GetMapping("/{query_id}")
    ResponseEntity<JsonNode> getQueries(@PathVariable("query_id") String queryId);

    @PostMapping
    ResponseEntity<JsonNode> postQueries(@RequestBody FunmanPostQueriesRequest requestBody);
}