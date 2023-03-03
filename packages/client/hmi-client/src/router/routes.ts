import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconCode32 from '@carbon/icons-vue/es/code/32';
import { AssetType } from '@/types/common';

export enum RouteName {
	HomeRoute = 'home',
	ProjectRoute = 'project',
	ProvenanceRoute = 'provenance',
	DataExplorerRoute = 'dataExplorer',
	CodeRoute = 'code'
}

export const RouteMetadata: {
	[key in RouteName]: { displayName: string; icon: any; projectAsset?: AssetType };
} = {
	[RouteName.ProjectRoute]: { displayName: 'Project summary', icon: IconAccount32 },
	[RouteName.CodeRoute]: {
		displayName: 'Code',
		icon: IconCode32,
		projectAsset: AssetType.CODE
	},
	[RouteName.ProvenanceRoute]: { displayName: 'Provenance', icon: IconFlow32 },
	[RouteName.HomeRoute]: { displayName: 'Home', icon: 'pi pi-home' },
	[RouteName.DataExplorerRoute]: { displayName: 'Explorer', icon: 'pi pi-compass' }
};
