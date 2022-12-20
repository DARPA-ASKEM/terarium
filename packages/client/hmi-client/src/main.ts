import { createApp } from 'vue';
import { createPinia } from 'pinia';
import useAuthStore from './stores/auth';
import router from './router';
import App from './App.vue';
import './assets/css/style.css';

const app = createApp(App);
app.use(createPinia());
app.use(router);

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
