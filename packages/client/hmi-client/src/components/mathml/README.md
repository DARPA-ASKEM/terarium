# TERA-MATH-EDITOR

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
