package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;

/**
 * Proxies the TDS code endpoints
 */
@FeignClient(name = "code", url = "${terarium.dataservice.url}", path = "/code")

public interface CodeProxy extends TDSProxy<Code> {

}
