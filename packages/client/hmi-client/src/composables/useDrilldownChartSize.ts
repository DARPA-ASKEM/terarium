import { Ref, ref } from 'vue';
import _ from 'lodash';
import { drilldownChartSize } from '@/components/workflow/util';
import { useResizeObserver } from './useResizeObserver';

/**
 * A Vue composable that provides reactive chart size based on the container element's size.
 *
 * @param {Ref<HTMLElement>} containerElement - A reference to the container HTML element.
 * @returns {Ref<{ width: number, height: number }>} - A reactive reference to the chart size.
 */
export function useDrilldownChartSize(containerElement: Ref<HTMLElement | null>, debounceDelay = 100) {
	const size = ref(drilldownChartSize(null));
	useResizeObserver(
		containerElement,
		_.debounce(() => {
			size.value = drilldownChartSize(containerElement.value);
		}, debounceDelay)
	);
	return size;
}
