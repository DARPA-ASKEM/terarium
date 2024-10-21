import { createApp } from 'vue';
import { RouteLocationNormalized } from 'vue-router';
import { createPinia } from 'pinia';
import ConfirmationService from 'primevue/confirmationservice';
import PrimeVue from 'primevue/config';
import ToastService from 'primevue/toastservice';
import Tooltip from 'primevue/tooltip';
import VueFeather from 'vue-feather';
import VueGtag from 'vue-gtag';
import { MathfieldElement } from 'mathlive';
import VueKatex from '@hsorby/vue3-katex';
import 'csstype';
import { EventType } from '@/types/Types';
import * as EventService from '@/services/event';
import API from '@/api/api';
import useAuthStore from '@/stores/auth';
import router from '@/router';
import App from '@/App.vue';
import { useProjects } from '@/composables/project';
import { useNotificationManager } from '@/composables/notificationManager';
import { init as clientEventServiceInit } from '@/services/ClientEventService';
import '@/assets/css/style.scss';

// Create the Vue application
const app = createApp(App);
// Set up the pinia store to be able to use for useAuthStore()
app.use(createPinia());

// Set up the Keycloak authentication
const authStore = useAuthStore();
await authStore.init();

// Initialize Client Events
await clientEventServiceInit();

// Set the hash value of the window.location to null.
// This is to prevent the Keycloak from redirecting to the hash value after authentication.
window.location.hash = '';

app
	.use(router)
	.use(ToastService)
	.use(ConfirmationService)
	.use(PrimeVue, { ripple: true })
	.use(VueKatex)
	.directive('tooltip', Tooltip)
	.directive('focus', {
		mounted(el) {
			el.focus();
		}
	});

// Configure Google Analytics
const GTAG = await API.get('/configuration/ga');
if (GTAG.data) {
	app.use(VueGtag, { config: { id: GTAG.data } });
}

app.component('math-field', MathfieldElement);
app.component(VueFeather.name ?? 'vue-feather', VueFeather);
app.mount('body');

let previousRoute: RouteLocationNormalized | null = null;
let routeStartedMillis = Date.now();
router.beforeEach(async (to, _from, next) => {
	if (previousRoute) {
		const nowMillis = Date.now();
		const timeSpent = nowMillis - routeStartedMillis;
		await EventService.create(
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

useNotificationManager().init();

// Allow the use of CSS custom properties
declare module 'csstype' {
	export interface Properties {
		[key: `--${string}`]: string | undefined;
	}
}
