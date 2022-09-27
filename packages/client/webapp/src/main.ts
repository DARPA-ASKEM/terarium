import { createApp } from 'vue';
import router from './router';
import './style.css';
import App from './App.vue';
import { getAccessToken, isAuthorized } from './utils/uncloak';

await getAccessToken();
if (isAuthorized()) {
	const app = createApp(App);
	app.use(router);
	app.mount('#app');
} else {
	console.error('FAILED TO AUTHORIZE');
}
