package software.uncharted.terarium.taskrunner.util;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ScopedLock {

	private ReentrantLock lock = new ReentrantLock();

	@FunctionalInterface
	public interface ThrowingRunnable<E extends Exception> {
		void run() throws E;
	}

	public <E extends Exception> void lock(ThrowingRunnable<E> task) throws E {
		lock.lock();
		try {
			task.run();
		} finally {
			lock.unlock();
		}
	}

	public <T> T lock(Supplier<T> supplier) {
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}
}
