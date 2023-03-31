# Development

Basic rules to write organised code.

## Folder organisation

### `page`

- list of folders matching the current pages of terarium.

### `components`

- only components used everywhere in terarium.
- or base components, which are extended in each `page`.

### `services`*

- domain specific to terarium;
- where calls to API are made;
- good place to unload big functions off a component.

### `utils`*

- generic functions;
- utilities that can be reused easily.

> Folders with an asterisk (*), every function within should have their own test.

## Naming convention

### Default

- All names should be `kebab-case`.

### Events

- context followed by the action
- _i.e._ `query-updated` or `document-added-to-project`

### Types

- types are in `PascalCase` and end with `Type`
- please use `interface` instead of `type` to define them
- _i.e._ `ModelType` or `DocumentNameType`

## Components

- Component name are preceded by `tera`

    _.i.e_ `/components/slider.vue` -> `<tera-slider>`

- Vue single-file architecture order is:

    ```html
    <template></template>
    <script lang=ts></script>
    <style scoped></style>
    ```

- Leverage HTML5 semantic tag to improve lisibility instead of `<div>` soup and weirdly named classnames.

    ```html
    <template>
        <main>                      <!-- <div class="component-container"> -->
            <header></header>           <!-- <div class="header"></div> -->
            <section></section>         <!-- <div></div> -->
            <section></section>         <!-- <div></div> -->
            <aside></aside>             <!-- <div class="sidebar"></div> -->
            <footer></footer>           <!-- <div class="footer"></div> -->
        </main>                     <!-- </div> -->
    </template>

    <style scoped>
        main {}                     /* .component-container {} */
        section {}                  /* .component-container > div {} */
        aside {}                    /* .sidebar {} */
    </style>
    ```

- Some components (e.g. `Model.vue`) will showcase an asset's attributes and allows users to edit them. However there are cases where we just want to show a more compact read-only version of these components. In these cases assign the flag `:is-editable="false"` for these components:

    ```html
    <model 
        :asset-id="previewItemId"
        :project="resources.activeProject" 
        :highlight="searchTerm"
        :is-editable="false"
    />
    ```

## `LOGGING & TOASTS`

### LOGGING

There is a new logging service in place that buffers log messages to be posted to the `/logs` hmi-server endpoint.  The HMI server then echos out the message for kibana to eventually consume.

In a dev environment, logs are displayed in the browser console as well.

An instance of the logger is created and exported once in `logger.ts`

```export const logger = new Logger({
 consoleEnabled: !isProduction,
 callerInfoEnabled: true,
 **showToast: false** // show all logs recorded as toasts - currently off by default.
});
```

Each log message can consume a `LoggerMessageOptionsType` object that can tell the service

```interface LoggerMessageOptionsType {
 showToast?: boolean; // will force a show or surpression of toast
 toastTitle?: string; // change default title of toast
 silent?: boolean; // do not transmit to backend
}
```

The toast service can also be used independent of the logger:

```import Toast from 'primevue/toast';
import { ToastSummaries, ToastSeverity, useToastService } from '@/services/toast';

const toast = useToastService();

toast.showToast(
   ToastSeverity.error,
   `${ToastSummaries.NETWORK_ERROR} (${status})`,
   'Unauthorized',
   5000
  );
```

## TERA-MATH-EDITOR

Terrarium uses [mathlive](https://cortexjs.io/docs/mathlive/) & [mathjax 2.7.2](https://docs.mathjax.org/en/v2.7-latest/start.html) via a the `vue-mathjax-next` component to create a custom component: `tera-math-editor`

This component `<tera-math-editor>` can display a `LaTeX` formatted formula using either renderer by setting `:mathmode` to a `MathEditorModes.JAX | MathEditorModes.LIVE`.

## Props

| Prop Name | Type | Required | Default Value | Description |
| --- | --- | --- | --- | --- |
| `latexEquation` | `String` | `true` | N/A | The formula to be rendered. |
| `mathMode` | `String` | `false` | `MathEditorModes.LIVE` | The math renderer mode. |
| `isEditable` | `Boolean` | `false` | `true` | Whether to show an edit button. |
| `isEditingEq` | `Boolean` | `false` | `true` | Whether the equation is being edited. |
| `showDevOptions` | `Boolean` | `false` | `false` | Whether to show the renderer selection box. |
| `isMathMlValid` | `Boolean` | `false` | `true` | Whether the MathML for the equation is valid. |

### `latexEquation`

The formula to be rendered, specified as a LaTeX string (but can also technically accept simple mathml). This prop is required and must be a non-empty string.

### `mathMode`

The math renderer mode to use. This prop is optional and defaults to `MathEditorModes.LIVE`. Possible values are:

* `MathEditorModes.LIVE`: Renders the formula MathLive.
* `MathEditorModes.JAX`: Renders the formula using MathJax.

### `isEditable`

Whether to show an edit button for the formula. This prop is optional and defaults to `true`. Set to `false` to hide the edit button.

### `isEditingEq`

Whether the equation is currently being edited. This prop is optional and defaults to `false`. Set to `true` to enable editing.

### `showDevOptions`

Whether to show the math renderer selection box. This prop is optional and defaults to `false`. Set to `true` to show the selection box.

### `isMathMlValid`

Whether the MathML for the equation is valid. This prop is optional and defaults to `true`. Set to `false` to show an error message.

## Emits

| Event Name | Arguments | Description |
| --- | --- | --- |
| `equation-updated` | `updatedEquation: string` | Emitted when the equation is updated. |
| `validate-mathml` | `isValid: boolean` | Emitted when the MathML is validated. |
| `cancel-editing` | N/A | Emitted when editing of the equation is cancelled. |

### `equation-updated`

Emitted when the equation is updated, with the updated equation passed as a string argument.

### `validate-mathml`

Emitted when trying to save the new mathml formula.  The mathml is validated and the `isMathMlValid` is properly set.

### `cancel-editing`

Emitted when editing of the equation is stopped. No arguments are passed.

## Usage

```html
<template>
  <div>
    <math-formula-renderer
      :latex-equation="latexEquation"
      :math-mode="mathMode"
      :is-editable="isEditable"
      :is-editing-eq="isEditingEq"
      :show-dev-options="showDevOptions"
      :is-math-ml-valid="isMathMlValid"
      @equation-updated="handleEquationUpdated"
      @validate-mathml="handleMathMLValidation"
      @stop-editing="handleStopEditing"
/>
```
