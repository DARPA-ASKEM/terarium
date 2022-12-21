import { createApp } from 'vue';
import { createPinia } from 'pinia';
import PrimeVue from 'primevue/config';
import useAuthStore from './stores/auth';
import router from './router';
import App from './App.vue';
import './assets/css/style.css';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(PrimeVue, { ripple: true });

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
