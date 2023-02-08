import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconAppConnectivity32 from '@carbon/icons-vue/es/app-connectivity/32';
import IconDocument32 from '@carbon/icons-vue/es/document/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconChartCombo32 from '@carbon/icons-vue/es/chart--combo/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconCode32 from '@carbon/icons-vue/es/code/32';
import { ProjectAssetTypes } from '@/types/Project';

export enum RouteName {
	DatasetRoute = 'dataset',
	DocumentRoute = 'document',
	HomeRoute = 'home',
	ModelRoute = 'model',
	ProjectRoute = 'project',
	ProvenanceRoute = 'provenance',
	SimulationRoute = 'simulation',
	SimulationResultRoute = 'simulationResult',
	DataExplorerRoute = 'dataExplorer',
	CodeRoute = 'code'
}

export const RouteMetadata: {
	[key in RouteName]: { displayName: string; icon: any; projectAsset?: ProjectAssetTypes };
} = {
	[RouteName.DatasetRoute]: {
		displayName: 'Data',
		icon: IconTableSplit32,
		projectAsset: ProjectAssetTypes.DATASETS
	},
	[RouteName.DocumentRoute]: {
		displayName: 'Documents',
		icon: IconDocument32,
		projectAsset: ProjectAssetTypes.PUBLICATIONS
	},
	[RouteName.ModelRoute]: {
		displayName: 'Models',
		icon: IconMachineLearningModel32,
		projectAsset: ProjectAssetTypes.MODELS
	},
	[RouteName.ProjectRoute]: { displayName: 'Project summary', icon: IconAccount32 },
	[RouteName.SimulationRoute]: {
		displayName: 'Workflows',
		icon: IconAppConnectivity32,
		projectAsset: ProjectAssetTypes.PLANS
	},
	[RouteName.SimulationResultRoute]: {
		displayName: 'Analysis',
		icon: IconChartCombo32,
		projectAsset: ProjectAssetTypes.SIMULATION_RUNS
	},
	[RouteName.CodeRoute]: {
		displayName: 'Code',
		icon: IconCode32,
		projectAsset: ProjectAssetTypes.CODE
	},
	[RouteName.ProvenanceRoute]: { displayName: 'Provenance', icon: IconFlow32 },
	[RouteName.HomeRoute]: { displayName: 'Home', icon: 'pi pi-home' },
	[RouteName.DataExplorerRoute]: { displayName: 'Explorer', icon: 'pi pi-compass' }
};
