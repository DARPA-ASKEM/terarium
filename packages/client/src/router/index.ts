import { createRouter, createWebHashHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';

const routes = [
	{ path: '/', component: Home },
	{ path: '/results', component: ResponsiveMatrixCells }
];

const router = createRouter({
	// 4. Provide the history implementation to use. We are using the hash history for simplicity here.
	history: createWebHashHistory(),
	routes // short for `routes: routes`
});

export default router;
