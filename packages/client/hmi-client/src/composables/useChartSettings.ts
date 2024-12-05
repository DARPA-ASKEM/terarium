import { ref, computed } from 'vue';
import { ChartSetting, ChartSettingEnsembleVariable, ChartSettingType } from '@/types/common';
import {
	addMultiVariableChartSetting,
	EnsembleVariableChartSettingOption,
	removeChartSettingById,
	updateChartSettingsBySelectedVariables,
	updateEnsembleVariableChartSettingOption
} from '@/services/chart-settings';
import { WorkflowNode } from '@/types/workflow';

/**
 * Composable to manage chart settings for a given workflow node.
 *
 * @param props - The properties passed to the composable, including the workflow node.
 * @param emit - The emit function to trigger events.
 *
 * @returns An object containing chart settings and methods to manipulate them.
 */
export function useChartSettings(
	props: { node: WorkflowNode<{ chartSettings: ChartSetting[] | null }> },
	emit: (event: 'update-state', ...args: any[]) => void
) {
	const chartSettings = computed(() => props.node.state.chartSettings ?? []);
	const activeChartSettings = ref<ChartSetting | null>(null);
	const comparisonChartsSettingsSelection = ref<string[]>([]);

	// Computed properties to filter chart settings by type
	const selectedParameterSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.DISTRIBUTION_COMPARISON)
	);
	const selectedInterventionSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.INTERVENTION)
	);
	const selectedVariableSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.VARIABLE)
	);
	const selectedEnsembleVariableSettings = computed(
		() =>
			chartSettings.value.filter(
				(setting) => setting.type === ChartSettingType.VARIABLE_ENSEMBLE
			) as ChartSettingEnsembleVariable[]
	);
	const selectedErrorVariableSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.ERROR_DISTRIBUTION)
	);
	const selectedComparisonChartSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.VARIABLE_COMPARISON)
	);

	const selectedSensitivityChartSettings = computed(() =>
		chartSettings.value.filter((setting) => setting.type === ChartSettingType.SENSITIVITY)
	);

	// Methods to manage chart settings
	const removeChartSettings = (chartId: string) => {
		emit('update-state', {
			...props.node.state,
			chartSettings: removeChartSettingById(chartSettings.value, chartId)
		});
	};

	const updateChartSettings = (selectedVariables: string[], type: ChartSettingType) => {
		emit('update-state', {
			...props.node.state,
			chartSettings: updateChartSettingsBySelectedVariables(chartSettings.value, type, selectedVariables)
		});
	};

	const addComparisonChartSettings = () => {
		emit('update-state', {
			...props.node.state,
			chartSettings: addMultiVariableChartSetting(
				chartSettings.value,
				ChartSettingType.VARIABLE_COMPARISON,
				comparisonChartsSettingsSelection.value
			)
		});
		comparisonChartsSettingsSelection.value = [];
	};

	const updateEnsembleVariableSettingOption = (option: EnsembleVariableChartSettingOption, value: boolean) => {
		emit('update-state', {
			...props.node.state,
			chartSettings: updateEnsembleVariableChartSettingOption(chartSettings.value, option, value)
		});
	};

	return {
		chartSettings,
		activeChartSettings,
		comparisonChartsSettingsSelection,
		selectedVariableSettings,
		selectedEnsembleVariableSettings,
		selectedErrorVariableSettings,
		selectedParameterSettings,
		selectedInterventionSettings,
		selectedComparisonChartSettings,
		selectedSensitivityChartSettings,
		removeChartSettings,
		updateChartSettings,
		addComparisonChartSettings,
		updateEnsembleVariableSettingOption
	};
}
