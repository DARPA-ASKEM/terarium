import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import { AssetType } from '@/types/Types';

export enum RouteName {
	Home = 'home',
	Project = 'project',
	Provenance = 'provenance',
	WorkflowNode = 'workflow-node',
	UserAdmin = 'user-admin'
}

export const RouteMetadata: {
	[key in RouteName]: { displayName: string; icon: any; projectAsset?: AssetType };
} = {
	[RouteName.Project]: { displayName: 'Project summary', icon: IconAccount32 },
	[RouteName.Provenance]: { displayName: 'Provenance', icon: IconFlow32 },
	[RouteName.Home]: { displayName: 'Home', icon: 'pi pi-home' },
	[RouteName.WorkflowNode]: { displayName: 'Workflow Node', icon: 'pi pi-bolt' },
	[RouteName.UserAdmin]: { displayName: 'User Administration', icon: 'pi pi-users' }
};
