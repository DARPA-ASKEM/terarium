/**
 * Observe the size change of an element and allow to add a callback
 *
 * @param element
 * @param callback
 * @returns ResizeObserver
 */
export function observeElementSizeChange(element: HTMLElement, callback: any) {
	// Create a ResizeObserver instance
	const resizeObserver = new ResizeObserver((entries) => {
		entries.forEach(callback);
	});

	// Start observing the target element
	resizeObserver.observe(element);
	return resizeObserver;
}
