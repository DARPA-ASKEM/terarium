import { logger } from '@/utils/logger';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ToastService from 'primevue/toastservice';
import PrimeVue from 'primevue/config';
import Tooltip from 'primevue/tooltip';
import VueFeather from 'vue-feather';
import { MathfieldElement } from 'mathlive';
import VueKatex from '@hsorby/vue3-katex';
import useAuthStore from './stores/auth';
import router from './router';
import '@node_modules/katex/dist/katex.min.css';
import App from './App.vue';

import './assets/css/style.scss';

export const app = createApp(App);
app.use(ToastService);
app.use(createPinia());
app.use(router);
app.use(PrimeVue, { ripple: true });
app.directive('tooltip', Tooltip);

app.component('math-field', MathfieldElement);
app.component(VueFeather.name, VueFeather);
app.use(VueKatex);

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
logger.info('Application Mounted', { showToast: false, silent: true });

// Allow the use of CSS custom properties
declare module '@vue/runtime-dom' {
	export interface CSSProperties {
		[key: `--${string}`]: string | undefined;
	}
}
