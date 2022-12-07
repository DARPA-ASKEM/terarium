import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconAppConnectivity32 from '@carbon/icons-vue/es/app-connectivity/32';
import IconDocument32 from '@carbon/icons-vue/es/document/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconChartCombo32 from '@carbon/icons-vue/es/chart--combo/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';

// This should be moved to Project.ts can't replace what's there because it'll break a lot of things can't have this and consts in the same file due to the no shadow rule
export enum ProjectAssetTypes {
	PUBLICATIONS = 'publications',
	INTERMEDIATES = 'intermediates',
	MODELS = 'models',
	PLANS = 'plans',
	SIMULATION_RUNS = 'simulation_runs',
	DATASETS = 'datasets'
}

export enum RouteName {
	DatasetRoute = 'dataset',
	DocumentRoute = 'document',
	HomeRoute = 'home',
	ModelRoute = 'model',
	ProfileRoute = 'profile',
	ProjectRoute = 'project',
	ProvenanceRoute = 'provenance',
	SimulationRoute = 'simulation',
	SimulationResultRoute = 'simulationResult'
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
		displayName: 'Papers',
		icon: IconDocument32,
		projectAsset: ProjectAssetTypes.PUBLICATIONS
	},
	[RouteName.ModelRoute]: {
		displayName: 'Models',
		icon: IconMachineLearningModel32,
		projectAsset: ProjectAssetTypes.MODELS
	},
	[RouteName.ProfileRoute]: { displayName: 'Profile', icon: IconUser32 },
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
	[RouteName.ProvenanceRoute]: { displayName: 'Provenance', icon: IconFlow32 },
	[RouteName.HomeRoute]: { displayName: 'Home', icon: null }
};
