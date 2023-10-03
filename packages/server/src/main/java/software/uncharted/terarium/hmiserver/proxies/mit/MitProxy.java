package software.uncharted.terarium.hmiserver.proxies.mit;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "mit", url = "${mit-proxy.url}")
public interface MitProxy {

	//TODO I question if these are all actually String?

	@PostMapping("/petri/get_places")
	ResponseEntity<String> getPlaces(
		@RequestParam("code") final String code);

	@PostMapping("/petri/get_transitions")
	ResponseEntity<String> getTransitions(
		@RequestParam("code") final String code);

	@PostMapping("/petri/get_arcs")
	ResponseEntity<String> getArcs(
		@RequestParam("code") final String code);

	@PostMapping("/petri/get_pyacset")
	ResponseEntity<String> getPyAcset(
		@RequestParam("places_str") final String places,
		@RequestParam("transitions_str") final String transitions,
		@RequestParam("arcs_str") final String arcs
	);

	@PostMapping("/annotation/find_text_vars")
	ResponseEntity<String> findTextVars(
		@RequestParam("async") final String async,
		@RequestParam("text") final String text
	);

	@PostMapping("/annotation/link_annos_to_pyacset")
	ResponseEntity<String> linkAnnotationsToAcset(
		@RequestParam("pyacset_str") final String pyacset,
		@RequestParam("annotations_str") final String annotations,
		@RequestParam("info_str") final String info
	);

	@PostMapping("/annotation/link_dataset_col_to_dkg")
	ResponseEntity<String> linkDatasetColToDKG(
		@RequestParam("csv_str") final String csv,
		@RequestParam("doc") final String doc
	);

	@GetMapping("/response")
	ResponseEntity<String> getResponse(@RequestParam("id") final String id);
}
