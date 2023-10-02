package software.uncharted.terarium.hmiserver.proxies.dataservice;

import org.springframework.cloud.openfeign.FeignClient;
import software.uncharted.terarium.hmiserver.models.dataservice.NotebookSession;

@FeignClient(name = "notebookSessions", url = "${terarium.dataservice.url}", path = "/notebook_sessions")
public interface NotebookSessionProxy extends TDSProxy<NotebookSession> {

}
