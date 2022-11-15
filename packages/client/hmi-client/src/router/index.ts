import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';
import TA2Playground from '@/views/TA2Playground.vue';
import SimulationPlanPlayground from '@/views/SimulationPlanPlayground.vue';
import TheiaView from '@/views/theia.vue';
import ProjectView from '@/components/projects/Project.vue';
import DocumentView from '@/views/document.vue';
import Simulation from '@/views/Simulation.vue';
import ProjectView from '@/components/projects/Project.vue';

export enum RoutePath {
	Home = '/',
	Results = '/results',
	Ta2Playground = '/ta2-playground',
	SimulationPlanPlaygroundPath = '/simulation-plan-playground',
	Theia = '/theia',
	Project = '/:id',
	DocView = '/docs/:id?',
	SimulationView = '/simulation',
	Project = '/:id'
}

const routes = [
	{ path: RoutePath.Home, component: HomeView },
	{ path: RoutePath.Results, component: ResponsiveMatrixCells },
	{ path: RoutePath.Ta2Playground, component: TA2Playground },
	{ path: RoutePath.SimulationPlanPlaygroundPath, component: SimulationPlanPlayground },
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.Project, component: ProjectView, props: true },
	{ path: RoutePath.DocView, component: DocumentView, props: true },
	{ path: RoutePath.SimulationView, component: Simulation },
	{ path: RoutePath.Project, component: ProjectView, props: true }
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
