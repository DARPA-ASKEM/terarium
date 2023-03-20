import { logger } from '@/utils/logger';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ToastService from 'primevue/toastservice';
import PrimeVue from 'primevue/config';
import Tooltip from 'primevue/tooltip';
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

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
logger.info('Application Mounted', { showToast: false, silent: true });
