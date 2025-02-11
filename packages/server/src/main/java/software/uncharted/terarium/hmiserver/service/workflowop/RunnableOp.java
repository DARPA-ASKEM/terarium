package software.uncharted.terarium.hmiserver.service.workflowop;

public abstract class RunnableOp implements Runnable {

	public abstract void handleRun() throws InterruptedException;

	public abstract void handleInterrupt();

	public abstract void handleError();

	@Override
	public void run() {
		try {
			handleRun();
		} catch (InterruptedException ie) {
			handleInterrupt();
		} catch (Exception e) {
			handleError();
		}
	}
}
