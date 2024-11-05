import _ from 'lodash';
import { Ref, ref, watch } from 'vue';
import { drilldownChartSize } from '@/components/workflow/util';
import { useResizeObserver } from './useResizeObserver';

/**
 * A Vue composable that provides reactive chart size based on the container element's size.
 *
 * @param {Ref<HTMLElement | null>} containerElement - A reference to the container HTML element.
 * @param {number} [debounceDelay=100] - The debounce delay in milliseconds for resize events.
 * @returns {Ref<{ width: number, height: number }>} - A reactive reference to the chart size.
 */
export function useDrilldownChartSize(containerElement: Ref<HTMLElement | null>, debounceDelay = 100) {
	const size = ref(drilldownChartSize(null));
	watch(containerElement, (val) => {
		// Init the size as soon as the container element is available
		if (!val) return;
		size.value = drilldownChartSize(containerElement.value);
	});
	useResizeObserver(
		containerElement,
		_.debounce(() => {
			// Handle resize and adjust the chart size
			size.value = drilldownChartSize(containerElement.value);
		}, debounceDelay)
	);
	return size;
}
