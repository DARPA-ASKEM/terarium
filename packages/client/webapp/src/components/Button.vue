<template>
	<button type="button"><slot /></button>
</template>

<style scoped>
button {
	--btn-color: var(--un-color-black-5);
	--btn-background: var(--un-color-black-50);
	--btn-border: var(--un-color-black-60);

	background: var(--btn-background);
	border: 1px solid var(--btn-border);
	border-radius: 4px;
	color: var(--btn-color);
	font: var(--un-font-large);
	min-width: 5em;
	outline: 0;
	padding: 0.5em 1em;
	transition-duration: 0.2s;
	transition-property: background;
	transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
}

/* Types */

button[action] {
	--btn-background: var(--un-color-accent);
	--btn-border: var(--un-color-accent-dark);
}

button[success] {
	--btn-background: var(--un-color-feedback-success);
	--btn-border: var(--un-color-feedback-success-dark);
}

button[info] {
	--btn-background: var(--un-color-feedback-info);
	--btn-border: var(--un-color-feedback-info-dark);
}

button[warning] {
	--btn-background: var(--un-color-feedback-warning);
	--btn-border: var(--un-color-feedback-warning-dark);
}

button[danger] {
	--btn-background: var(--un-color-feedback-danger);
	--btn-border: var(--un-color-feedback-danger-dark);
}
</style>
