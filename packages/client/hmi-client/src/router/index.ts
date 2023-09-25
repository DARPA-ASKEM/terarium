import { computed } from 'vue';
import { createRouter, createWebHashHistory } from 'vue-router';
import HomeView from '@/page/Home.vue';
import DataExplorerView from '@/page/data-explorer/DataExplorer.vue';
import UnauthorizedView from '@/page/Unauthorized.vue';
import ProjectView from '@/page/project/tera-project.vue';
import WorkflowNodeView from '@/page/WorkflowNode.vue';

// These are test/experiment pages
import ResponsivePlayground from '@/temp/ResponsivePlayground.vue';
import TheiaView from '@/temp/theia.vue';
import SSE from '@/temp/sse.vue';
import AMRPetriTest from '@/temp/AMRPetriTest.vue';
import PyodideTest from '@/temp/PyodideTest.vue';
import { RouteName } from './routes';

export enum RoutePath {
	Home = '/',
	Project = `/projects/:projectId/:pageType?/:assetId?`,
	WorkflowNode = `/${RouteName.WorkflowNode}/:workflowId/:nodeId`,
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
	{ name: RouteName.Home, path: RoutePath.Home, component: HomeView },
	{
		name: RouteName.Project,
		path: RoutePath.Project,
		component: ProjectView,
		props: true
	},
	{
		name: RouteName.WorkflowNode,
		path: RoutePath.WorkflowNode,
		component: WorkflowNodeView,
		props: true
	},
	{
		name: RouteName.DataExplorer,
		path: RoutePath.DataExplorer,
		component: DataExplorerView
	},

	// Playground and experiments, these components are testing-only
	{ path: RoutePath.Theia, component: TheiaView },
	{ path: RoutePath.ResponsivePlaygroundPath, component: ResponsivePlayground },
	{ path: '/sse', component: SSE },
	{ path: '/amr-petri-test', component: AMRPetriTest },
	{ path: '/pyodide-test', component: PyodideTest }
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
