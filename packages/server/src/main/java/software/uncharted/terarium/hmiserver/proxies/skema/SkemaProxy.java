package software.uncharted.terarium.hmiserver.proxies.skema;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import software.uncharted.terarium.hmiserver.models.code.CodeRequest;

@FeignClient(name = "skema-py", url = "${skema-py.url}")
public interface SkemaProxy {
	/**
	 * Converts a {@link CodeRequest} to a function network via TA1 Skema
	 *
	 * @param request the {@link CodeRequest} instance containing the code snippit
	 * @return an escaped JSON string of the function network
	 */
	@PostMapping("/fn-given-filepaths")
	ResponseEntity<String> getFunctionNetwork(@RequestBody CodeRequest request);
}
