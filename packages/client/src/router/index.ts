import { createRouter, createWebHistory } from 'vue-router';
import Home from '@/views/Home.vue';
import ResponsiveMatrixCells from '@/components/ResponsiveMatrixCells.vue';

const routes = [
	{ path: '/', component: Home },
	{ path: '/results', component: ResponsiveMatrixCells }
];

const router = createRouter({
	// Provide the history implementation to use. We are using HTML5 Mode
	// https://router.vuejs.org/guide/essentials/history-mode.html#html5-mode
	history: createWebHistory(),

	// short for `routes: routes`
	routes
});

export default router;
