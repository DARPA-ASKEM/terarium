package software.uncharted.terarium.hmiserver.controller.funman;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;
import software.uncharted.terarium.hmiserver.models.funman.requests.FunmanPostQueries;
import software.uncharted.terarium.hmiserver.models.funman.responses.FunmanGetQueriesResponse;
import software.uncharted.terarium.hmiserver.models.funman.responses.FunmanPostQueriesResponse;

@RestController
@RequestMapping("/funman/queries")
public class FunmanController {

    @Autowired
    private FunmanProxy funmanProxy;

    @GetMapping("/{query_id}/halt")
    public ResponseEntity<JsonNode> halt(@PathVariable String query_id) {
        ResponseEntity<JsonNode> response = funmanProxy.halt(query_id);

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/{query_id}")
    public ResponseEntity<FunmanGetQueriesResponse> getQueries(@PathVariable String query_id) {
        ResponseEntity<FunmanGetQueriesResponse> response = funmanProxy.getQueries(query_id);

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping
public ResponseEntity<FunmanPostQueriesResponse> postQueries(@RequestBody FunmanPostQueries requestBody) {
    ResponseEntity<FunmanPostQueriesResponse> response = funmanProxy.postQueries(requestBody);

    return ResponseEntity.ok(response.getBody());
}
}
