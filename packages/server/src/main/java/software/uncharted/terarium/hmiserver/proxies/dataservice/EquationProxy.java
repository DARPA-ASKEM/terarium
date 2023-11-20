package software.uncharted.terarium.hmiserver.proxies.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;

import java.util.List;

/**
 * Feignclient representation for TDS's Equation endpoints
 */
@FeignClient(name = "equations", url = "${terarium.dataservice.url}", path = "/equations")
public interface EquationProxy {

    /**
     * Retrieve the list of Equations
     * @param pageSize number of equations per page
     * @param page current page number
     * @return list of equations
     */
    @GetMapping
    ResponseEntity<List<Equation>> getEquations(
            @RequestParam(name = "page_size", defaultValue = "100") Integer pageSize,
            @RequestParam(name = "page", defaultValue = "0") Integer page
    );

    /**
     * Create equation and return its ID
     * @param equation equation to create
     * @return new ID for equation
     */
    @PostMapping
    ResponseEntity<ResponseId> createEquation(@RequestBody Equation equation);

    /**
     * Retrieve an equation
     * @param id equation id
     * @return Equation
     */
    @GetMapping("/{equation_id}")
    ResponseEntity<Equation> getEquation(@PathVariable("equation_id") String id);

    /**
     * Update an equation
     * @param id id of equation to update
     * @param equation equation to update with
     * @return ID of updated equation
     */
    @PutMapping("/{equation_id}")
    ResponseEntity<ResponseId> updateEquation(@PathVariable("equation_id") String id, @RequestBody Equation equation);

    /**
     * Deletes and equation
     * @param id equation to delete
     * @return delete message
     */
    @DeleteMapping("/{equation_id}")
    ResponseEntity<JsonNode> deleteEquation(@PathVariable("equation_id") String id);

}
