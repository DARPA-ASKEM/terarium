import { createRouter, createWebHashHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';
import TA2Playground from '@/views/TA2Playground.vue';
import Theia from '@/views/theia.vue';

export const HOME_PATH = '/';

const routes = [
	{ path: HOME_PATH, component: Home },
	{ path: '/results', component: ResponsiveMatrixCells },
	{ path: '/ta2-playground', component: TA2Playground },
	{ path: '/theia', component: Theia }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),

	// short for `routes: routes`
	routes
});

export default router;
