package software.uncharted.terarium.hmiserver.proxies.mira;

import feign.FeignException;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import software.uncharted.terarium.hmiserver.models.mira.DKG;

@FeignClient(name = "mira-api", url = "${mira-api.url}", path = "/api")
public interface MIRAProxy {
	@PostMapping("/search")
	ResponseEntity<List<DKG>> search(@RequestBody String q, @RequestParam Integer limit, @RequestParam Integer offset)
		throws FeignException;
}
