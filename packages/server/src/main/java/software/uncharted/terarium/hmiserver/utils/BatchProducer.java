package software.uncharted.terarium.hmiserver.utils;

public interface BatchProducer<T> {
	void onBatch(T batch);

	default void onError(Throwable t) {}
}
