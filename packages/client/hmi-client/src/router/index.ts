import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import DocumentView from '@/views/Document.vue';
import HomeView from '@/views/Home.vue';
import DatasetView from '@/views/Dataset.vue';
import ProjectView from '@/views/Project.vue';
import ModelView from '@/views/ModelView.vue';
import ResponsivePlayground from '@/views/ResponsivePlayground.vue';
import SimulationPlanPlayground from '@/views/SimulationPlanPlayground.vue';
import SimulationView from '@/views/Simulation.vue';
import SimulationResultView from '@/views/SimulationResult.vue';
import TA2Playground from '@/views/TA2Playground.vue';
import TheiaView from '@/views/theia.vue';
import DataExplorerView from '@/views/DataExplorer.vue';
import { RouteName } from './routes';

export enum RoutePath {
	Home = '/',
	Project = '/projects/:projectId',
	Document = '/projects/:projectId/docs/:assetId?',
	Model = '/projects/:projectId/model/:assetId?',
	Dataset = '/projects/:projectId/dataset/:assetId?',
	Simulation = '/projects/:projectId/simulations/:assetId?',
	SimulationResult = '/projects/:projectId/simulation-results/:assetId?',
	DataExplorer = '/data-explorer',

	// Playground and experiments, these components are testing-only
	Theia = '/theia',
	Ta2Playground = '/ta2-playground',
	ResponsivePlaygroundPath = '/responsive-playground',
	SimulationPlanPlaygroundPath = '/simulation-plan-playground'
}

const routes = [
	{ name: RouteName.DocumentRoute, path: RoutePath.Document, component: DocumentView, props: true },
	{ name: RouteName.HomeRoute, path: RoutePath.Home, component: HomeView },
	{ name: RouteName.ModelRoute, path: RoutePath.Model, component: ModelView, props: true },
	{ name: RouteName.DatasetRoute, path: RoutePath.Dataset, component: DatasetView, props: true },
	{ name: RouteName.ProjectRoute, path: RoutePath.Project, component: ProjectView },
	{ name: RouteName.SimulationRoute, path: RoutePath.Simulation, component: SimulationView },
	{
		name: RouteName.SimulationResultRoute,
		path: RoutePath.SimulationResult,
		component: SimulationResultView
	},
	{
		path: RoutePath.DataExplorer,
		component: DataExplorerView,
		props: (route) => ({ query: route.query.q })
	},
	// Playground and experiments, these components are testing-only
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.Ta2Playground, component: TA2Playground },
	{ path: RoutePath.ResponsivePlaygroundPath, component: ResponsivePlayground },
	{ path: RoutePath.SimulationPlanPlaygroundPath, component: SimulationPlanPlayground }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),

	// short for `routes: routes`
	routes
});

export function useCurrentRouter() {
	return {
		isCurrentRouteHome: computed(() => router.currentRoute.value.path === RoutePath.Home)
	};
}

export default router;
