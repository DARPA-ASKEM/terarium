package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.models.dataservice.ResponseId;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.proxies.dataservice.EquationProxy;
import software.uncharted.terarium.hmiserver.security.Roles;

import java.util.List;

/**
 * Rest controller for storing, retrieving, modifying and deleting equation assets in the dataservice
 */
@RequestMapping("/equations")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EquationController {

    final EquationProxy equationProxy;

    /**
     * Retrieve the list of Equations
     * @param pageSize number of equations per page
     * @param page current page number
     * @return list of equations
     */
    @GetMapping
    @Secured(Roles.USER)
    ResponseEntity<List<Equation>> getEquations(
            @RequestParam(name = "page_size", defaultValue = "100") Integer pageSize,
            @RequestParam(name = "page", defaultValue = "0") Integer page
    ) {
        return ResponseEntity.ok(equationProxy.getEquations(pageSize, page).getBody());
    }

    /**
     * Create equation and return its ID
     * @param equation equation to create
     * @return new ID for equation
     */
    @PostMapping
    @Secured(Roles.USER)
    ResponseEntity<ResponseId> createEquation(@RequestBody Equation equation){
        return ResponseEntity.ok(equationProxy.createEquation(equation).getBody());
    }

    /**
     * Retrieve an equation
     * @param id equation id
     * @return Equation
     */
    @GetMapping("/{equation_id}")
    @Secured(Roles.USER)
    ResponseEntity<Equation> getEquation(@PathVariable("equation_id") String id){
        return ResponseEntity.ok(equationProxy.getEquation(id).getBody());
    }

    /**
     * Update an equation
     * @param id id of equation to update
     * @param equation equation to update with
     * @return ID of updated equation
     */
    @PutMapping("/{equation_id}")
    @Secured(Roles.USER)
    ResponseEntity<ResponseId> updateEquation(@PathVariable("equation_id") String id, @RequestBody Equation equation){
        return ResponseEntity.ok(equationProxy.updateEquation(id, equation).getBody());
    }

    /**
     * Deletes and equation
     * @param id equation to delete
     * @return delete message
     */
    @DeleteMapping("/{equation_id}")
    @Secured(Roles.USER)
    ResponseEntity<JsonNode> deleteEquation(@PathVariable("equation_id") String id){
        return ResponseEntity.ok(equationProxy.deleteEquation(id).getBody());
    }


}
