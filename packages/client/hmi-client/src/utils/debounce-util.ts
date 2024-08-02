/**
 * Ensures that the provided function isn't called too many times in a short period of time,
 * which can be useful for performance optimizations like reducing the number of discarded search
 * responses when the user types quickly.
 *
 * Usage:
 * ```
 * // Wrap the expensive function once like so:
 * function myExpensiveFetch() {...}
 * const debouncedFetch = debounce(myExpensiveFetch);
 *
 * // Then call the wrapped version
 * <input @keyup="debouncedFetch" />
 * ```
 * @param callback The function to be debounced.
 * @param wait The number of milliseconds to wait after the wrapper function is called before actually calling the callback. Defaults to 500ms.
 * @returns A debounced version of the `callback` function.
 */
export default function debounce<T extends Function>(callback: T, wait = 500) {
	let h: NodeJS.Timeout | undefined;
	const debouncedFunction = (...args: any) => {
		clearTimeout(h);
		h = setTimeout(() => callback(...args), wait);
	};
	// Assert that the wrapped function as the same type as the original function
	return debouncedFunction as unknown as T;
}
