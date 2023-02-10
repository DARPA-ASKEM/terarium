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
config.server.fs = {
	allow: ['.', '/graph-scaffolder']
};
config.server.proxy = {
	'/api': {
		target: `http://${process.env.hmi_server_host || 'hmi-server'}:${
			process.env.hmi_server_port || 3000
		}`,
		changeOrigin: true
	}
};

export default config;
