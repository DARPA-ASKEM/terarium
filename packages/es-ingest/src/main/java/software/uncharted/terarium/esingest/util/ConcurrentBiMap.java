package software.uncharted.terarium.esingest.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBiMap<K, V> {

	private final ReentrantLock lock = new ReentrantLock();

	ConcurrentMap<K, V> keyToValue = new ConcurrentHashMap<>();
	ConcurrentMap<V, K> valueToKey = new ConcurrentHashMap<>();

	public boolean containsKey(K key) {
		lock.lock();
		try {
			return keyToValue.containsKey(key);
		} finally {
			lock.unlock();
		}
	}

	public boolean containsValue(V val) {
		lock.lock();
		try {
			return valueToKey.containsKey(val);
		} finally {
			lock.unlock();
		}
	}

	public void put(K k, V v) {
		lock.lock();
		try {
			keyToValue.put(k, v);
			valueToKey.put(v, k);
		} finally {
			lock.unlock();
		}
	}

	public V get(K k) {
		lock.lock();
		try {
			return keyToValue.get(k);
		} finally {
			lock.unlock();
		}
	}

	public K getKey(V v) {
		lock.lock();
		try {
			return valueToKey.get(v);
		} finally {
			lock.unlock();
		}
	}

}
