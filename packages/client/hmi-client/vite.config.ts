/// <reference types="vitest" />
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import svgLoader from 'vite-svg-loader';
import * as path from 'path';

// const node_modules_dir = path.resolve(__dirname, '../../../node_modules/');
const node_modules_dir = path.resolve(
	__dirname,
	process.env.COMPOSED !== 'true' ? '../../../node_modules/' : './node_modules/'
);

/**
 * Vite Configuration
 *
 * Please update any changes in `playwright-ct.config.ts` under `config.use.ctViteConfig`.
 */

// See: https://vitejs.dev/config/
// See: https://vitejs.dev/config/server-options.html#server-proxy
export default defineConfig({
	optimizeDeps: {
		exclude: ['mathlive']
	},
	// Syntax sugar for specifying imports
	resolve: {
		alias: [
			{ find: /^~/, replacement: node_modules_dir + '/' },
			{ find: 'path', replacement: path.resolve(node_modules_dir, 'path-browserify/') },
			{ find: '@', replacement: path.resolve(__dirname, './src') },
			{ find: '@assets', replacement: path.resolve(__dirname, './src/assets') },
			{ find: '@node_modules', replacement: node_modules_dir },
			{ find: '@graph-scaffolder', replacement: path.resolve(__dirname, '../graph-scaffolder/src') }
		]
	},
	server: {
		port: 8080,
		strictPort: true,
		// Due to the reverse proxy being present the following
		// HMR port option is set as per NOTE in the docs
		// https://vitejs.dev/config/server-options.html#server-hmr
		hmr: {
			port: 8080
		},
		proxy: {
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
		}
	},
	preview: {
		port: 8080
	},
	// See https://stackoverflow.com/questions/76051452/rollup-vite-build-error-when-including-geotiff-js-and-web-worker-in-build
	worker: {
		format: 'es'
	},
	build: {
		target: 'esnext',
		rollupOptions: {
			input: {
				main: path.resolve(__dirname, 'index.html'),
				sso: path.resolve(__dirname, 'silent-sso.html')
			}
		}
	},
	plugins: [
		vue({
			template: {
				compilerOptions: {
					// treat all components starting with `facet` as custom elements
					// ignore facets as custom elements
					isCustomElement: (tag) => tag.startsWith('facet-') || tag === 'math-field' || tag == 'katex'
				}
			}
		}),
		// By default SVGs are imported as URL in order to easily reference them in img tags
		// In order to import SVGs as components you must add '?component' as a suffix of an SVG's path
		svgLoader({ defaultImport: 'url' })
	],
	test: {
		include: ['tests/unit/**/*.{test,spec}.{ts,mts}'],
		reporters: ['junit', 'default'],
		outputFile: {
			junit: './tests/unit/reports/junit-report.xml'
		},
		environment: 'jsdom'
	}
});
