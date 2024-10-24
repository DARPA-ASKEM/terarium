/**
 * Simple FIFO-based cache
 * */
export class FIFOCache<T> {
	private limit: number;

	private cache: Map<string, T> = new Map();

	constructor(size = 50) {
		this.limit = size;
	}

	get(key: string) {
		return this.cache.get(key);
	}

	set(key: string, value: T) {
		this.cache.set(key, value);

		// Expunge, keys are in insertion order thus making it fifo
		if (this.cache.size > this.limit) {
			const firstKey = this.cache.keys().next().value;
			if (firstKey) this.cache.delete(firstKey);
		}
	}
}
