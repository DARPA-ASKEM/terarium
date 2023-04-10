import { logger } from '@/utils/logger';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ToastService from 'primevue/toastservice';
import PrimeVue from 'primevue/config';
import Tooltip from 'primevue/tooltip';
import VueFeather from 'vue-feather';
import { MathfieldElement } from 'mathlive';
import VueMathjax from 'vue-mathjax-next';
import useAuthStore from './stores/auth';
import router from './router';
import App from './App.vue';

import './assets/css/style.scss';

export const app = createApp(App);
app.use(ToastService);
app.use(createPinia());
app.use(router);
app.use(PrimeVue, { ripple: true });
app.directive('tooltip', Tooltip);
app.use(VueMathjax); // we need to intialize this vue mathjax component this way
app.component('math-field', MathfieldElement);
app.component(VueFeather.name, VueFeather);

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
logger.info('Application Mounted', { showToast: false, silent: true });
