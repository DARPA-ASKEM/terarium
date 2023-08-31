import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '@/page/Home.vue';
import DataExplorerView from '@/page/data-explorer/DataExplorer.vue';
import UnauthorizedView from '@/page/Unauthorized.vue';
import ProjectView from '@/page/project/tera-project.vue';

// These are test/experiment pages
import ResponsivePlayground from '@/temp/ResponsivePlayground.vue';
import TheiaView from '@/temp/theia.vue';
import SSE from '@/temp/sse.vue';
import EvaluationScenarios from '@/temp/EvaluationScenarios.vue';
import AMRPetriTest from '@/temp/AMRPetriTest.vue';
import PyodideTest from '@/temp/PyodideTest.vue';
import NewTeraModel from '@/components/model/tera-model.vue';
import { RouteName } from './routes';

export enum RoutePath {
	Home = '/',
	Project = '/projects/:projectId/:pageType?/:assetId?',
	DataExplorer = '/explorer',
	Unauthorized = '/unauthorized',

	// Playground and experiments, these components are testing-only
	Theia = '/theia',
	Ta2Playground = '/ta2-playground',
	ResponsivePlaygroundPath = '/responsive-playground',
	EvaluationScenariosPath = '/evaluation-scenarios',
	ModelEditor = '/model-editor',
	ModelRunner = '/model-runner'
}

const routes = [
	{ name: 'unauthorized', path: RoutePath.Unauthorized, component: UnauthorizedView },
	{ name: RouteName.HomeRoute, path: RoutePath.Home, component: HomeView },
	{
		name: RouteName.ProjectRoute,
		path: RoutePath.Project,
		component: ProjectView,
		props: true
	},
	{
		name: RouteName.DataExplorerRoute,
		path: RoutePath.DataExplorer,
		component: DataExplorerView
	},
	// Playground and experiments, these components are testing-only
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.ResponsivePlaygroundPath, component: ResponsivePlayground },
	{ path: RoutePath.EvaluationScenariosPath, component: EvaluationScenarios },
	{ path: '/sse', component: SSE },
	{ path: '/amr-petri-test', component: AMRPetriTest },
	{ path: '/pyodide-test', component: PyodideTest },
	{ path: '/new-tera-model', component: NewTeraModel }
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
