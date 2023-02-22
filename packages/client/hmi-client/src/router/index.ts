import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import DocumentView from '@/page/DocumentView.vue';
import HomeView from '@/page/Home.vue';
import DatasetView from '@/page/DatasetView.vue';
import ModelView from '@/page/ModelView.vue';
import ResponsivePlayground from '@/page/ResponsivePlayground.vue';
import SimulationView from '@/page/Simulation.vue';
import SimulationResultView from '@/page/SimulationResult.vue';
import CodeView from '@/page/CodeView.vue';
import TA2Playground from '@/page/TA2Playground.vue';
import TheiaView from '@/page/theia.vue';
import DataExplorerView from '@/page/data-explorer/DataExplorer.vue';
import UnauthorizedView from '@/page/Unauthorized.vue';
import ModelEditorView from '@/page/ModelEditor.vue';
import ModelRunnerView from '@/page/ModelRunner.vue';
import ProjectView from '@/page/project/project.vue';
import { RouteName } from './routes';

export enum RoutePath {
	Home = '/',
	Project = '/projects/:projectId/resources/:resourceType?/:assetId?',
	Document = '/projects/:projectId/docs/:assetId?',
	Model = '/projects/:projectId/model/:assetId?',
	Dataset = '/projects/:projectId/dataset/:assetId?',
	Simulation = '/projects/:projectId/simulations/:assetId?',
	SimulationResult = '/projects/:projectId/simulation-results/:assetId?',
	Code = '/projects/:projectId/code',
	DataExplorer = '/explorer',
	Unauthorized = '/unauthorized',

	// Playground and experiments, these components are testing-only
	Theia = '/theia',
	Ta2Playground = '/ta2-playground',
	ResponsivePlaygroundPath = '/responsive-playground',
	ModelEditor = '/model-editor',
	ModelRunner = '/model-runner'
}

const routes = [
	{ name: 'unauthorized', path: RoutePath.Unauthorized, component: UnauthorizedView },
	{ name: RouteName.DocumentRoute, path: RoutePath.Document, component: DocumentView, props: true },
	{ name: RouteName.HomeRoute, path: RoutePath.Home, component: HomeView },
	{
		name: RouteName.ProjectRoute,
		path: RoutePath.Project,
		component: ProjectView,
		props: true
	},
	{ name: RouteName.ModelRoute, path: RoutePath.Model, component: ModelView, props: true },
	{ name: RouteName.DatasetRoute, path: RoutePath.Dataset, component: DatasetView, props: true },
	{ name: RouteName.SimulationRoute, path: RoutePath.Simulation, component: SimulationView },
	{
		name: RouteName.SimulationResultRoute,
		path: RoutePath.SimulationResult,
		component: SimulationResultView
	},
	{
		name: RouteName.DataExplorerRoute,
		path: RoutePath.DataExplorer,
		component: DataExplorerView,
		props: (route) => ({ query: route.query.q })
	},
	{ name: RouteName.SimulationRoute, path: RoutePath.Simulation, component: SimulationView },
	{ name: RouteName.CodeRoute, path: RoutePath.Code, component: CodeView },
	// Playground and experiments, these components are testing-only
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.Ta2Playground, component: TA2Playground },
	{ path: RoutePath.ResponsivePlaygroundPath, component: ResponsivePlayground },
	{ path: RoutePath.ModelEditor, component: ModelEditorView },
	{ path: RoutePath.ModelRunner, component: ModelRunnerView }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),

	// short for `routes: routes`
	routes
});

export function useCurrentRoute() {
	return computed(() => router.currentRoute.value);
}

export default router;
