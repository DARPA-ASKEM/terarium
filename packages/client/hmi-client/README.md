# Guidelines
- [Development](DEVELOPMENT.md)
- [Testing](TEST.md)

# Setup
## Vue 3 + TypeScript + Vite

- Terarium uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.
- CSS utility library is [PrimeFlex](https://www.primefaces.org/primeflex/).
- Components System is [PrimeVue](https://primefaces.org/primevue).
- Icons is [Feather Icons](https://vue-feather-icons.egoist.dev/).

## Recommended IDE Setup

- [VS Code](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar)
- Vim is also a ~~reasonable~~ valid choice

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
