import { createRouter, createWebHashHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';
import DataExplorer from '@/views/DataExplorer.vue';
import Theia from '@/views/theia.vue';

const routes = [
	{ path: '/', component: Home },
	{ path: '/results', component: ResponsiveMatrixCells },
	{ path: '/explorer', component: DataExplorer },
	{ path: '/theia', component: Theia }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),

	// short for `routes: routes`
	routes
});

export default router;
