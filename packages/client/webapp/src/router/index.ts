import { createRouter, createWebHashHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';
import DataExplorer from '@/views/DataExplorer.vue';
import TA2Playground from '@/views/TA2Playground.vue';

const routes = [
	{ path: '/', component: Home },
	{ path: '/results', component: ResponsiveMatrixCells },
	{ path: '/explorer', component: DataExplorer },
	{ path: '/ta2-playground', component: TA2Playground }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),

	// short for `routes: routes`
	routes
});

export default router;
