import { onUnmounted, Ref, watch } from 'vue';

/**
 * A composable function that sets up a ResizeObserver to observe changes in the size of a target element.
 *
 * @param targetElement - A Vue ref object containing the HTMLElement to be observed.
 * @param callback - A callback function that will be called with the new DOMRectReadOnly object whenever the size of the target element changes.
 *
 * @example
 * ```typescript
 * import { ref } from 'vue';
 * import { useResizeObserver } from './useResizeObserver';
 *
 * const elementRef = ref<HTMLElement>(null);
 *
 * useResizeObserver(elementRef, (rect) => {
 *   console.log('Element size changed:', rect);
 * });
 * ```
 */
export function useResizeObserver(targetElement: Ref<HTMLElement | null>, callback: (rect: DOMRectReadOnly) => void) {
	const resizeObserver = new ResizeObserver((entries) => {
		entries.forEach((entry) => callback(entry.contentRect));
	});

	watch(
		targetElement,
		(val, oldVal) => {
			// In case the target element changes, stop observing the old element and start observing the new one.
			if (oldVal) resizeObserver.unobserve(oldVal);
			if (val) resizeObserver.observe(val);
		},
		{ immediate: true }
	);

	onUnmounted(() => {
		// Stop observing the target element when the component is unmounted
		if (targetElement.value) resizeObserver.unobserve(targetElement.value);
		resizeObserver.disconnect();
	});
}
