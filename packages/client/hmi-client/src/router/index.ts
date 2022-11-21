import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';
import TA2Playground from '@/views/TA2Playground.vue';
import SimulationPlanPlayground from '@/views/SimulationPlanPlayground.vue';
import TheiaView from '@/views/theia.vue';
import DocumentView from '@/views/document.vue';
import Simulation from '@/views/Simulation.vue';
import Model from '@/views/Model.vue';

export enum RoutePath {
	Home = '/',

	DocView = '/docs/:id?',
	ModelView = '/projects/:projectId/model',
	SimulationView = '/projects/:projectId/simulation',
	Results = '/projects/:projectId/results',

	// Playground and experiments, these components are testing-only
	Theia = '/theia',
	Ta2Playground = '/ta2-playground',
	SimulationPlanPlaygroundPath = '/simulation-plan-playground'
}

// Named routes
export enum RouteName {
	HomeRoute = 'home',
	SimulationRoute = 'simulation',
	ModelRoute = 'model',

	DatasetRoute = 'dataset',
	DocumentRoute = 'document',
	ProvenanceRoute = 'provenance',
	ProfileRoute = 'profile'
}

const routes = [
	{ name: RouteName.HomeRoute, path: RoutePath.Home, component: HomeView },
	{ name: RouteName.SimulationRoute, path: RoutePath.SimulationView, component: Simulation },
	{ name: RouteName.ModelRoute, path: RoutePath.ModelView, component: Model },

	// TODO
	{ path: RoutePath.Results, component: ResponsiveMatrixCells },
	{ path: RoutePath.DocView, component: DocumentView, props: true },

	// Playground and experiments, these components are testing-only
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.Ta2Playground, component: TA2Playground },
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
