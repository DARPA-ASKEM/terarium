import { fetchAnnotations } from '@/services/chart-settings';
import { ChartAnnotation, ClientEventType } from '@/types/Types';
import { onMounted, ref } from 'vue';
import { useClientEvent } from './useClientEvent';

/**
 * Fetches chart annotations for a given node id and stores them in a ref.
 * The ref is updated when a new annotation is created or deleted and can be accessed by getChartAnnotationsByChartId getter function.
 * @param nodeId
 */
export function useChartAnnotations(nodeId: string) {
	const chartAnnotations = ref<ChartAnnotation[]>([]);
	const updateChartAnnotations = async () => {
		chartAnnotations.value = await fetchAnnotations(nodeId);
	};
	onMounted(() => updateChartAnnotations());
	useClientEvent(
		[ClientEventType.ChartAnnotationCreate, ClientEventType.ChartAnnotationDelete],
		updateChartAnnotations
	);

	const getChartAnnotationsByChartId = (id: string) =>
		chartAnnotations.value.filter((annotation) => annotation.chartId === id);

	return {
		getChartAnnotationsByChartId
	};
}
