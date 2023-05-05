/// <reference types="vitest" />
import * as path from 'path';
import config from './vite.config';

/**
 * Vite Configuration
 *
 * Please update any changes in `playwright-ct.config.ts` under `config.use.ctViteConfig`.
 */

// See: https://vitejs.dev/config/
// See: https://vitejs.dev/config/server-options.html#server-proxy

// Configuration overrides for running inside docker-compose
config.resolve.alias['@node_modules'] = path.resolve(__dirname, './node_modules');
config.resolve.alias['~@lumino'] = path.resolve(__dirname, './node_modules/@lumino/');
config.resolve.alias['~@jupyterlab'] = path.resolve(__dirname, './node_modules/@jupyterlab/');
config.resolve.alias['~@blueprintjs'] = path.resolve(__dirname, './node_modules/@blueprintjs/');
config.resolve.alias['~codemirror'] = path.resolve(__dirname, './node_modules/codemirror/');
config.resolve.alias['~@fortawesome'] = path.resolve(__dirname, './node_modules/@fortawesome/');
config.server.fs = {
	allow: ['.', '/graph-scaffolder']
};
config.server.proxy = {
	'/api': {
		target: `http://${process.env.hmi_server_host || 'hmi-server'}:${
			process.env.hmi_server_port || 3000
		}`,
		changeOrigin: true
	},
	'/chatty_ws': {
		target: `ws://${process.env.jupyter_host || 'jupyter'}:${process.env.jupyter_port || 8888}`,
		changeOrigin: true,
		rewrite: (path_str) => path_str.replace(/^\/chatty_ws/, ''),
		ws: true
	},
	'/chatty': {
		target: `http://${process.env.jupyter_host || 'jupyter'}:${process.env.jupyter_port || 8888}`,
		changeOrigin: true,
		rewrite: (path_str) => path_str.replace(/^\/chatty/, ''),
		ws: true
	}
};
// Fix HMR port to match port set in docker compose. https://vitejs.dev/config/server-options.html#server-hmr
config.server.hmr = {
	clientPort: 8078
};

export default config;
