import esbuild from 'esbuild';
import yargs from 'yargs';
import { globbySync } from 'globby';
import path from 'path';
import copy from 'copy';
import server from 'live-server';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

function liveServer(options = {}) {
    const defaultParams = {
        file: 'index.html',
        host: '0.0.0.0',
        logLevel: 2,
        open: false,
        port: 8080,
        root: '.',
        wait: 200,
    };

    const params = Object.assign({}, defaultParams, options);
    let running = false;

    return {
        start() {
            if (!running) {
                running = true;
                server.start(params);
                console.log(`live-server running on ${params.port}`);
            }
        },
    };
}

function pluginIgnoreImports(options) {
    const config = Object.assign({
        ignoreFilter: /.*/,
        allowFilePatterns: [ /.(?:css)$/ ],
        allowFolderPatterns: [],
    }, options);

    function shouldAllow(args) {
        if (args.kind === 'entry-point') {
            return true;
        }

        for (const pattern of config.allowFolderPatterns) {
            if (args.resolveDir.match(pattern)) {
                return true;
            }
        }

        for (const pattern of config.allowFilePatterns) {
            if (args.path.match(pattern)) {
                return true;
            }
        }

        return false;
    }

    return {
        name: 'ignore-imports',
        setup(build) {
            build.onResolve({ filter: config.ignoreFilter }, args => {
                if (!shouldAllow(args)) {
                    return {
                        path: args.path,
                        external: true,
                    };
                }
                return undefined;
            });
        },
    };
}

function getExamplesBuild() {
    return {
        entryPoints: [ 'examples/src/index.ts' ],
        bundle: true,
        outdir: 'build/examples/examples',
        target: 'es2020',
        format: 'esm',
        sourcemap: true,
        plugins: [],
    };
}

function getLibBuild() {
    const input = [];
    globbySync([
        path.join('src/', '/**/*.{ts,js}'),
        `!${path.join('src/', '/**/*.d.ts')}`,
    ]).forEach(file => {
        input.push(file);
    });

    return {
        entryPoints: input,
        bundle: true,
        outdir: 'build/lib/',
        target: 'es2020',
        format: 'esm',
        sourcemap: true,
        plugins: [
            pluginIgnoreImports(),
        ],
    };
}

function getDistBuild() {
    return {
        entryPoints: [ 'src/index.ts' ],
        bundle: true,
        outdir: 'build/dist/',
        target: 'es2020',
        format: 'esm',
        sourcemap: false,
        minify: true,
        plugins: [],
    };
}

async function main(options) {
    // const promises = [];
    try {
			  let context = null;
        if (options.examples) {
						console.log('[build]: examples');
            context = await esbuild.context(getExamplesBuild());
						await context.rebuild();
						if (options.watch) {
							console.log('hihi');
							await context.watch()
						}
        }

        if (options.lib || options.all) {
						console.log('[build]: lib');
            context = await esbuild.context(getLibBuild());
						await context.rebuild();
						context.dispose();
        }

        if (options.dist || options.all) {
						console.log('[build]: dist');
            context = await esbuild.context(getDistBuild());
						await context.rebuild();
						context.dispose();
        }

        if (options.examples) {
            copy('examples/static/**/*', 'build/examples/', (err) => {
                if (err) {
										console.log('error', err);
										throw new Error(err);
                }
            });
        }

        if (options['dev-server']) {
            const server = liveServer({
                port: 8090,
                host: '0.0.0.0',
                root: path.resolve(__dirname, 'examples/static/'),
                file: 'index.html',
                open: false,
                wait: 500,
                // proxy: [['/api', 'http://127.0.0.1:8080']], // not needed for now, used to proxy to the server API
                watch: [
                    path.resolve(__dirname, 'examples/static'),
                    path.resolve(__dirname, 'build/examples/'),
                ],
                mount: [
                    ['/examples', path.resolve(__dirname, 'build/examples/examples')],
                ],
            });
            server.start();
        }
    } catch (e) {
        console.error(e);
        process.exit(1);
    }
}

main(yargs(process.argv).argv);
