<template>
	<button type="button"><slot /></button>
</template>

<style scoped>
button {
	--btn-color: var(--un-color-black-5);
	--btn-bg: var(--un-color-black-50);
	--btn-border: var(--un-color-black-60);
	background-color: var(--btn-bg);
	border: 2px solid var(--btn-border);
	border-radius: 4px;
	box-shadow: var(--un-box-shadow-small);
	color: var(--btn-color);
	font: var(--un-font-large);
	min-width: 5em;
	outline: 0;
	padding: 0.5em 1em;
}

/* Types */

button[action] {
	--btn-color: var(--un-color-body-text-primary);
	--btn-bg: var(--un-color-accent);
	--btn-border: var(--un-color-accent-dark);
}

button[success] {
	--btn-color: var(--un-color-feedback-success-darker);
	--btn-bg: var(--un-color-feedback-success);
	--btn-border: var(--un-color-feedback-success-darker);
}

button[info] {
	--btn-color: var(--un-color-feedback-info-darker);
	--btn-bg: var(--un-color-feedback-info);
	--btn-border: var(--un-color-feedback-info-darker);
}

button[warning] {
	--btn-color: var(--un-color-feedback-warning-darker);
	--btn-bg: var(--un-color-feedback-warning);
	--btn-border: var(--un-color-feedback-warning-darker);
}

button[danger] {
	--btn-color: var(--un-color-feedback-danger-darker);
	--btn-bg: var(--un-color-feedback-danger);
	--btn-border: var(--un-color-feedback-danger-darker);
}

/*

@mixin uncharted-button($color, $background, $border-color) {
	$focus-color: darken($border-color, 5%);
	$active-color: darken($border-color, 10%);
	background: linear-gradient(
		to bottom,
		lighten($background, 2%),
		darken($background, 2%)
	);
	border-color: $border-color;
	color: $color;
	transition-property: background;
	transition-duration: 0.2s;
	transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
	&:hover {
		background: linear-gradient(
			to bottom,
			darken($background, 2%),
			darken($background, 7%)
		);
		color: $color;
		border-color: $border-color;
	}
	&:focus {
		background: linear-gradient(
			to bottom,
			$background,
			darken($background, 5%)
		);
		color: $color;
	}
	&:active {
		background: linear-gradient(to bottom, $border-color, $border-color);
		color: $color;
		border-color: $active-color;
	}
}
*/
</style>
