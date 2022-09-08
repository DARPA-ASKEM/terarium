import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

// See: https://vitejs.dev/config/
// See: https://vitejs.dev/config/server-options.html#server-proxy
export default defineConfig({
	// Syntax sugar for specifying imports
	resolve: {
		alias: {
			'@': path.resolve(__dirname, './src')
		}
	},
	// Server proxy - change here to connect to API server (e.g. staging environment)
	server: {
		proxy: {
			'/api': {
				target: 'http://localhost:3000/api',
				changeOrigin: true,
				rewrite: (_path) => _path.replace(/^\/api/, '')
			}
		}
	},
	plugins: [vue()]
});
