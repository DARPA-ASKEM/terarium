import { logger } from '@/utils/logger';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import axios from 'axios';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';
import PrimeVue from 'primevue/config';
import Tooltip from 'primevue/tooltip';
import VueFeather from 'vue-feather';
import VueGtag from 'vue-gtag';
import { MathfieldElement } from 'mathlive';
import VueKatex from '@hsorby/vue3-katex';
import { EventType } from '@/types/Types';
import * as EventService from '@/services/event';
import Keycloak, { KeycloakOnLoad } from 'keycloak-js';
import useAuthStore from './stores/auth';
import router, { RoutePath } from './router';
import '@node_modules/katex/dist/katex.min.css';
import App from './App.vue';
import { useProjects } from './composables/project';

import './assets/css/style.scss';

export const app = createApp(App);
app.use(ToastService);
app.use(createPinia());
app.use(router);
app.use(ConfirmationService);
app.use(PrimeVue, { ripple: true });
app.directive('tooltip', Tooltip);

// Configure Google Analytics
const GTAG = await axios.get('/api/configuration/ga');
app.use(
	VueGtag,
	{
		config: {
			id: GTAG.data
		}
	},
	router
);

app.component('math-field', MathfieldElement);
app.component(VueFeather.name, VueFeather);
app.use(VueKatex);

const authStore = useAuthStore();
const keycloak = new Keycloak('/api/keycloak/config');
authStore.setKeycloak(keycloak);

await keycloak
	.init({
		onLoad: 'login-required' as KeycloakOnLoad
	})
	.then(async (auth) => {
		if (!auth) {
			window.location.reload();
		} else {
			await authStore.init();
			app.use(router);
			app.mount('#app');

			// Token Refresh
			setInterval(async () => {
				await keycloak.updateToken(70);
			}, 6000);
		}
	})
	.then(() => {
		app.mount('body');
		logger.info('Application Mounted', { showToast: false, silent: true });
		router.push(RoutePath.Home);
	})
	.catch((e) => {
		console.error('Authentication Failed', e);
	});

let previousRoute;
let routeStartedMillis = Date.now();
router.beforeEach((to, _from, next) => {
	if (previousRoute) {
		const nowMillis = Date.now();
		const timeSpent = nowMillis - routeStartedMillis;
		EventService.create(
			EventType.RouteTiming,
			useProjects().activeProject.value?.id,
			JSON.stringify({
				name: previousRoute.name,
				path: previousRoute.path,
				fullPath: previousRoute.fullPath,
				timeSpent
			})
		);
	}
	previousRoute = to;
	routeStartedMillis = Date.now();
	next();
});

// Allow the use of CSS custom properties
declare module '@vue/runtime-dom' {
	export interface CSSProperties {
		[key: `--${string}`]: string | undefined;
	}
}
