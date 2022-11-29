import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import DocumentView from '@/views/Document.vue';
import HomeView from '@/views/Home.vue';
import ModelView from '@/views/Model.vue';
import ProjectView from '@/views/Project.vue';
import ResponsivePlayground from '@/views/ResponsivePlayground.vue';
import SimulationPlanPlayground from '@/views/SimulationPlanPlayground.vue';
import SimulationView from '@/views/Simulation.vue';
import TA2Playground from '@/views/TA2Playground.vue';
import TheiaView from '@/views/theia.vue';

export enum RoutePath {
	Home = '/',
	Document = '/docs/:id?',
	Project = '/projects/:projectId?',
	Model = '/projects/:projectId/model/:modelId?',
	Simulation = '/projects/:projectId/simulation?',
	Results = '/projects/:projectId/results',

	// Playground and experiments, these components are testing-only
	Theia = '/theia',
	Ta2Playground = '/ta2-playground',
	ResponsivePlaygroundPath = '/responsive-playground',
	SimulationPlanPlaygroundPath = '/simulation-plan-playground'
}

// Named routes
export enum RouteName {
	DatasetRoute = 'dataset',
	DocumentRoute = 'document',
	HomeRoute = 'home',
	ModelRoute = 'model',
	ProfileRoute = 'profile',
	ProjectRoute = 'project',
	ProvenanceRoute = 'provenance',
	SimulationRoute = 'simulation'
}

const routes = [
	{ name: RouteName.DocumentRoute, path: RoutePath.Document, component: DocumentView, props: true },
	{ name: RouteName.HomeRoute, path: RoutePath.Home, component: HomeView },
	{ name: RouteName.ModelRoute, path: RoutePath.Model, component: ModelView, props: true },
	{ name: RouteName.ProjectRoute, path: RoutePath.Project, component: ProjectView },
	{ name: RouteName.SimulationRoute, path: RoutePath.Simulation, component: SimulationView },

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
