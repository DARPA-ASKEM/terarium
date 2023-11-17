package software.uncharted.terarium.hmiserver.controller.funman;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.proxies.funman.FunmanProxy;
import software.uncharted.terarium.hmiserver.models.funman.FunmanPostQueriesRequest;
import software.uncharted.terarium.hmiserver.security.Roles;

@RestController
@RequestMapping("/funman/queries")
public class FunmanController {

    @Autowired
    private FunmanProxy funmanProxy;

    @GetMapping("/{queryId}/halt")
    @Secured(Roles.USER)
    public ResponseEntity<JsonNode> halt(@PathVariable String queryId) {
        ResponseEntity<JsonNode> response = funmanProxy.halt(queryId);

        return ResponseEntity.ok(response.getBody());
    }

    @GetMapping("/{queryId}")
    @Secured(Roles.USER)
    public ResponseEntity<JsonNode> getQueries(@PathVariable String queryId) {
        ResponseEntity<JsonNode> response = funmanProxy.getQueries(queryId);

        return ResponseEntity.ok(response.getBody());
    }

    @PostMapping
    @Secured(Roles.USER)
    public ResponseEntity<JsonNode> postQueries(
        @RequestBody final FunmanPostQueriesRequest requestBody
    ) {
        ResponseEntity<JsonNode> response = funmanProxy.postQueries(requestBody);
        return ResponseEntity.ok(response.getBody());
    }
}
