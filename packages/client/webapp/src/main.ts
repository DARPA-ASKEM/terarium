import { createApp } from 'vue';
import { createPinia } from 'pinia';

import router from './router';
import App from './App.vue';

import './assets/css/style.css';
import '@fortawesome/fontawesome-free/css/all.css';
// eslint-disable-next-line import/extensions
import '@fortawesome/fontawesome-free/js/all.js';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('body');
