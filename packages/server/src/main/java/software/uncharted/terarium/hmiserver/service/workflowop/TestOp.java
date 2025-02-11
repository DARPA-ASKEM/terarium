package software.uncharted.terarium.hmiserver.service.workflowop;

public class TestOp extends RunnableOp {

	@Override
	public void handleRun() throws InterruptedException {
		for (int i = 0; i < 20; i++) {
			Thread.sleep(2000);
			System.out.println(">> " + i);
		}
	}

	@Override
	public void handleInterrupt() {
		System.out.println("Clean up ........ ");
	}

	@Override
	public void handleError() {
		handleInterrupt();
	}
}
