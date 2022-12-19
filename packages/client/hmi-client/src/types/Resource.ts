import { Project } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { RouteParamsRaw } from 'vue-router';

export type Resource = {
	route: RouteName;
	params: RouteParamsRaw;
	name: string;
	icon: any;
	projectAsset: Project;
};
