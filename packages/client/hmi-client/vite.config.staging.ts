/// <reference types="vitest" />
import config from './vite.config';

config.server.proxy = {
	'^/api': {
		target: 'https://server.staging.terarium.ai',
		rewrite: (path_str) => path_str.replace(/^\/api/, ''),
		changeOrigin: true
	},
	'^/beaker': {
		target: 'https://beaker.staging.terarium.ai',
		changeOrigin: true,
		rewrite: (path_str) => path_str.replace(/^\/beaker/, '')
	},
	'^/beaker_ws': {
		target: 'ws://beaker.staging.terarium.ai',
		ws: true,
		changeOrigin: true,
		rewrite: (path_str) => path_str.replace(/^\/beaker/, '')
	}
};

export default config;
