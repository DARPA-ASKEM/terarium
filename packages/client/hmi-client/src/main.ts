import ConfirmationService from 'primevue/confirmationservice';
import Keycloak, { KeycloakOnLoad } from 'keycloak-js';
import PrimeVue from 'primevue/config';
import ToastService from 'primevue/toastservice';
import Tooltip from 'primevue/tooltip';
import VueFeather from 'vue-feather';
import VueGtag from 'vue-gtag';
import axios from 'axios';
import { MathfieldElement } from 'mathlive';
import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from '@/App.vue';
import * as EventService from '@/services/event';
import VueKatex from '@hsorby/vue3-katex';
import router, { RoutePath } from '@/router';
import useAuthStore from '@/stores/auth';
import { EventType } from '@/types/Types';
import { logger } from '@/utils/logger';
import { useProjects } from '@/composables/project';

import '@/assets/css/style.scss';
import '@node_modules/katex/dist/katex.min.css';

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
const keycloak = new Keycloak('/api/configuration/keycloak');
authStore.setKeycloak(keycloak);

keycloak
	.init({
		onLoad: 'login-required' as KeycloakOnLoad
	})
	.then(async (auth) => {
		if (!auth) {
			window.location.reload();
		} else {
			await authStore.init();
			logger.info('Authenticated');

			app.use(router);
			app.mount('body');
			logger.info('Application Mounted', { showToast: false, silent: true });

			if (!router.currentRoute.value.name) {
				router.push(RoutePath.Home);
			}

			// Token Refresh
			setInterval(async () => {
				await keycloak.updateToken(70);
			}, 6000);
		}
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
