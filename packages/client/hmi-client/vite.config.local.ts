/// <reference types="vitest" />
import config from './vite.config';

// Configuration overrides for running local services via docker-compose
const localhost = process.env.local_host_name || 'localhost';
config.preview.port = 8080;
config.server.port = 80;
config.server.hmr.port = 80;
config.server.hmr.clientPort = 8080;
config.server.proxy = {
	'^/api': {
		target: `http://${localhost}:3000`,
		rewrite: (path_str) => path_str.replace(/^\/api/, ''),
		changeOrigin: true
	},
	'^/beaker_ws': {
		target: `ws://${localhost}:3050`,
		rewrite: (path_str) => path_str.replace(/^\/beaker_ws/, ''),
		changeOrigin: true,
		ws: true
	},
	'^/beaker': {
		target: `http://${localhost}:3050`,
		rewrite: (path_str) => path_str.replace(/^\/beaker/, ''),
		changeOrigin: true
	}
};

export default config;
