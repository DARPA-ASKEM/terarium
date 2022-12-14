import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconAppConnectivity32 from '@carbon/icons-vue/es/app-connectivity/32';
import IconDocument32 from '@carbon/icons-vue/es/document/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconChartCombo32 from '@carbon/icons-vue/es/chart--combo/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';

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

export const RouteMetadata: { [key in RouteName]: { displayName: string; icon: any } } = {
	[RouteName.DatasetRoute]: { displayName: 'Data', icon: IconTableSplit32 },
	[RouteName.DocumentRoute]: { displayName: 'Papers', icon: IconDocument32 },
	[RouteName.ModelRoute]: { displayName: 'Models', icon: IconMachineLearningModel32 },
	[RouteName.ProfileRoute]: { displayName: 'Profile', icon: IconUser32 },
	[RouteName.ProjectRoute]: { displayName: 'Project summary', icon: IconAccount32 },
	[RouteName.SimulationRoute]: { displayName: 'Workflows', icon: IconAppConnectivity32 },
	[RouteName.SimulationResultRoute]: { displayName: 'Analysis', icon: IconChartCombo32 },
	[RouteName.ProvenanceRoute]: { displayName: 'Provenance', icon: IconFlow32 },
	[RouteName.HomeRoute]: { displayName: 'Home', icon: null }
};
