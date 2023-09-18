package software.uncharted.terarium.hmiserver.proxies.dataservice;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;

import java.util.List;

@FeignClient(name = "datasets", url = "${terarium.dataservice.url}", path = "/datasets")
public interface DatasetProxy extends TDSProxy<Dataset>{


    @GetMapping("/{id}")
    Dataset getDataset(
            @PathVariable("id") String id
    );

}
