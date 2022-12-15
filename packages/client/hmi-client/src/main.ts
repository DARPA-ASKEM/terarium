import { createApp } from 'vue';
import { createPinia } from 'pinia';
import 'vuetify/styles';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import useAuthStore from './stores/auth';
import router from './router';
import App from './App.vue';
import './assets/css/style.css';

const app = createApp(App);
app.use(createPinia());
app.use(router);

const vuetify = createVuetify({ components, directives });
app.use(vuetify);

const auth = useAuthStore();
await auth.fetchSSO();

app.mount('body');
