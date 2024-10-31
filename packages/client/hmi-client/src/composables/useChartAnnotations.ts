import { fetchAnnotations } from '@/services/chart-settings';
import { ChartAnnotation, ClientEventType } from '@/types/Types';
import { onMounted, ref } from 'vue';
import { useClientEvent } from './useClientEvent';

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
		chartAnnotations,
		getChartAnnotationsByChartId
	};
}
