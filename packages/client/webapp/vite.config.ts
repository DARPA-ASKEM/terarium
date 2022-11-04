/// <reference types="vitest" />
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import * as path from 'path';

// See: https://vitejs.dev/config/
// See: https://vitejs.dev/config/server-options.html#server-proxy
export default defineConfig({
	// Syntax sugar for specifying imports
	resolve: {
		alias: {
			'@': path.resolve(__dirname, './src'),
			'@assets': path.resolve(__dirname, './src/assets'),
			'@node_modules': path.resolve(__dirname, '../../../node_modules'),
			'@graph-scaffolder': path.resolve(__dirname, '../graph-scaffolder/src')
		}
	},
	base: '/app/',
	server: {
		port: 8080,
		strictPort: true,
		// Due to the reverse proxy being present the following
		// HMR port option is set as per NOTE in the docs
		// https://vitejs.dev/config/server-options.html#server-hmr
		hmr: {
			port: 8080
		}
	},
	preview: {
		port: 8080
	},
	plugins: [
		vue({
			template: {
				compilerOptions: {
					// treat all components starting with `facet` as custom elements
					// ignore facets as custom elements
					isCustomElement: (tag) => tag.startsWith('facet-')
				}
			}
		})
	],
	test: {
		include: ['tests/unit/**/*.{test,spec}.{ts,mts}']
	}
});
