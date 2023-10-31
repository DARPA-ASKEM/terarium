package software.uncharted.terarium.hmiserver.controller.funman;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;
import software.uncharted.terarium.hmiserver.models.funman.FunmanPostQueriesRequest;

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
    public ResponseEntity<JsonNode> getQueries(@PathVariable String query_id) {
        ResponseEntity<JsonNode> response = funmanProxy.getQueries(query_id);

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping
    public ResponseEntity<JsonNode> postQueries(@RequestBody FunmanPostQueriesRequest requestBody) {
        ResponseEntity<JsonNode> response = funmanProxy.postQueries(requestBody);

        return ResponseEntity.ok(response.getBody());
    }
} 