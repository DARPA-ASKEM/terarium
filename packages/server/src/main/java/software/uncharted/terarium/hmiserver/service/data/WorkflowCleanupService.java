package software.uncharted.terarium.hmiserver.service.data;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowEdge;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.WorkflowNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowCleanupService {

	private final RedissonClient redissonClient;
	private final WorkflowService workflowService;
	private RLock lock;

	@PostConstruct
	void init() {
		lock = redissonClient.getLock("cleaner_upper_lock_name");
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady(final ApplicationReadyEvent event) {
		try {
			if (lock.tryLock(1, 100, TimeUnit.SECONDS)) {
				try {
					final Set<Workflow> workflows = workflowService.findWorkflowsToClean();
					for (final Workflow workflow : workflows) {
						workflow.getEdges().removeIf(WorkflowEdge::getIsDeleted);
						workflow.getNodes().removeIf(WorkflowNode::getIsDeleted);
					}
					workflowService.updateWorkflows(workflows);
				} catch (final IOException e) {
					throw new RuntimeException(e);
				} finally {
					lock.unlock();
				}
			} else {
				log.info("Someone else is cleaning this up");
			}
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
