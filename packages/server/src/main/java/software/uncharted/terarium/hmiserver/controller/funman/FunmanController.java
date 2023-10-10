package software.uncharted.terarium.hmiserver.controller.funman;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;

@RestController
@RequestMapping("/funman/queries")
public class FunmanController {

    @Autowired
    private FunmanProxy funmanProxy;

    @GetMapping("/{query_id}/halt")
    public ResponseEntity<JsonNode> halt(@PathVariable String query_id) {
        ResponseEntity<JsonNode> response = funmanProxy.halt(query_id);

        return response;
    }

    @GetMapping("/{query_id}")
    public ResponseEntity<JsonNode> getQueries(@PathVariable String query_id) {
        ResponseEntity<JsonNode> response = funmanProxy.getQueries(query_id);

        return response;
    }

    @PostMapping
    public ResponseEntity<JsonNode> postQueries(@RequestBody JsonNode requestBody) {
        ResponseEntity<JsonNode> response = funmanProxy.postQueries(requestBody);

        // Check and override the status code if it's 404
        // if(response.getStatusCode().value() == 404) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBody());
        // }

        return response;
    }
}
