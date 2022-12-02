# Vue 3 + TypeScript + Vite

- TERArium uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.
- We are using IBM Carbon Icons:
  - Check how to use them [within Vue](https://github.com/carbon-design-system/carbon/tree/v10/packages/icons-vue).
  - The reference [icons library](https://carbondesignsystem.com/guidelines/icons/library).

## Recommended IDE Setup

- [VS Code](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar)

## HTTP Requests

You can import and use the singleton [axios](https://axios-http.com/) instance to make requests to the `hmi-server` package like so:

```ts
import API from '@/api/api';

// ... in an async function:
const response = await API.get('models');
```

This will be routed through the gateway layer to avoid CORS issues.

## Type Support For `.vue` Imports in TS

Since TypeScript cannot handle type information for `.vue` imports, they are shimmed to be a generic Vue component type by default. In most cases this is fine if you don't really care about component prop types outside of templates. However, if you wish to get actual prop types in `.vue` imports (for example to get props validation when using manual `h(...)` calls), you can enable Volar's Take Over mode by following these steps:

1. Run `Extensions: Show Built-in Extensions` from VS Code's command palette, look for `TypeScript and JavaScript Language Features`, then right click and select `Disable (Workspace)`. By default, Take Over mode will enable itself if the default TypeScript extension is disabled.
2. Reload the VS Code window by running `Developer: Reload Window` from the command palette.

You can learn more about Take Over mode [here](https://github.com/johnsoncodehk/volar/discussions/471).

# Testing

This project is set up to use [Playwright](https://playwright.dev/) as its End-to-End (E2E) testing. Playwright is a modern E2E testing framework for web apps that tests multiple browsers, with full context isolation. The tests can be run manually through the CLI or if using Visual Studio Code, can be integrated using the official [Playwright Extension](https://marketplace.visualstudio.com/items?itemName=ms-playwright.playwright). Additionally the project uses GitLab CI/CD to automatically run the tests.

> If tests include screenshot capture then they also need to be updated for CI/CD use cases running on Linux as they will not match snapshots generated under Windows or OSX.

For unit testing the project uses [Vitest](https://vitest.dev/). Vitest is focused on delivering the best DX possible for lightning fast, headless testing. It uses built-in [Chai](https://www.chaijs.com/) for assertions and [Jest Expect](https://jestjs.io/docs/expect) compatible APIs. See [Vitest Info](#vitest) for more details.

## Playwright

### Configuration

The global configuration for `Playwright` is found at the project root in the [playwright.config.js](playwright.config.js) file. This file sets up the various global parameters as well the main test type projects. The configuration is set with reasonable defaults for most use cases but refer to the official [Documentation](https://playwright.dev/docs/intro) for full list of options.

> Note that these defaults can also be overridden at the test and test suite level if needed.

`Playwright` has been configured to automatically start or reuse the existing development server on port `5173`. If the test started the server then upon completion of the tests the server _should_ exit automatically, however on the occasion that something goes wrong you can use the following to find the process id running on that port and kill it manually. If the server is reused the server _should_ maintain active.

```sh
# NOTE: This command was tested on OSX only.
sudo lsof -i :4000
kill -9 {PID}
```

### Running E2E Tests

Tests can be run from within Visual Studio Code (if [extension](https://marketplace.visualstudio.com/items?itemName=ms-playwright.playwright) has been installed) or through the CLI. It is recommended to use the extension as it will greatly reduce the effort needed to efficiently run and debug the tests.

#### Extension

Using the [Playwright](https://marketplace.visualstudio.com/items?itemName=ms-playwright.playwright) extension allows for easier running and debugging of tests. All tests will be available in the `Testing` panel where one can run individual or all tests. Additionally within the test spec file, each test will have a <span style="color:green">green arrow</span> that can be clicked directly.

For debugging simply set up a breakpoint in the test and run the test in debug mode and the test will pause at that location.

> Note that the timeouts will continue so tests may fail just sitting on a breakpoint

#### Creating Tests

To create a new test simply make a new `xzy.spec.ts` file inside the `tests/e2e/` folder, import

```ts
import { test, expect } from '@playwright/test';
```

and write your tests.
Example

```ts
import { test, expect } from '@playwright/test';

test('basic test', async ({ page }) => {
	await page.goto('https://playwright.dev/');
	await expect(page).toHaveTitle(/Playwright/);
});
```

#### Codegen

To help bootstrap your E2E tests you can use `Playwrights` code generation mode to help stub out the sequence of events for the test. This can be invoked either by using the `Record` button present in the `Testing` panel within the extension, or through the CLI by invoking

```sh
npx playwright codegen
```

#### CLI

There are many commands that are supported for `Playwright`, below is a list of the most commonly used ones. Again refer to the official [Documentation](https://playwright.dev/docs/intro) for details, specifically the [CLI Docs](https://playwright.dev/docs/test-cli). To see the inline help run `npx playwright test --help`.

```sh
#Run all the tests
npx playwright test

#Run a single test file
npx playwright test tests/todo-page.spec.ts

#Run a set of test files
npx playwright test tests/todo-page/ tests/landing-page/

#Run files that have my-spec or my-spec-2 in the file name
npx playwright test my-spec my-spec-2

#Run the test with the title
npx playwright test -g "add a todo item"

#Run tests in headed browsers
npx playwright test --headed

#Run tests in a particular configuration (project)
npx playwright test --project=firefox

#Disable parallelization
npx playwright test --workers=1

#Choose a reporter
npx playwright test --reporter=dot

#Run in debug mode with Playwright Inspector
npx playwright test --debug

#Run in CodeGeneration mode
npx playwright codegen
```

### Component Testing

Component testing is also configured through `Playwright` and works similarly to `E2E` tests. The configuration for component tests are in a separate configuration found in [playwright-ct.config.ts](playwright-ct.config.ts). It is configured to look for tests in the `tests/component` directory and should be suffixed with the `spec` name as other tests (ie `foo.spec.ts`). For further information and configuration options refer to the [official documentation](https://playwright.dev/docs/test-components).

> NOTE: Component testing is still in experimental stage and may change in the future. As such there are [limitations](https://github.com/microsoft/playwright/issues/14298) of the framework currently.

To run the component tests within the project simply run:

```sh
yarn test:ct
```

To create a new test simply make a new `[component-name].spec.ts` file inside the `tests/component/` folder, import

```ts
import { test, expect } from '@playwright/experimental-ct-vue';
```

and write your tests.
Example

```ts
import { test, expect } from '@playwright/experimental-ct-vue';
import Foo from '@/components/Foo.vue';

test.describe('test Foo component', () => {
	test('should display the correct title', async ({ mount }) => {
		const component = await mount(Foo);
		const title = await component.locator('h1');
		await expect(title).toBeVisible();
	});
});
```

## Vitest

Vitest is a blazing fast unit test framework powered by Vite. It uses Vite's config, transformers, resolvers, and plugins with smart and instant watch mode. Can perform component testing for Vue, React, Svelte, Lit and more with out-of-box TypeScript / JSX support. Has built-int [Chai](https://www.chaijs.com/) support for assertions and [Jest Expect](https://jestjs.io/docs/expect) compatible APIs along with [Jest Snaphot](https://jestjs.io/docs/snapshot-testing) capabilities. Provided with built-in mocking and navtive code coverage via [c8](https://github.com/bcoe/c8).

To get started refer to the official [Documentation](https://vitest.dev/guide/).

To run the tests within this project simply run:

```sh
yarn test
```

> NOTE: By default this will launch the tests in `watch` mode, if a single run is needed only modify the script in `package.json` to be `vitest run`. See [Commands](https://vitest.dev/guide/#commands) for details.

To generate a coverage report, simply run:

```sh
yarn test:coverage
```

The report will be displayed in the terminal as well as a full HTML report will be generated in the `coverage` folder.
