<template>
	<button type="button"><slot /></button>
</template>

<style scoped>
button {
	background-color: var(--un-color-black-50);
	border: 0;
	border-radius: 4px;
	box-shadow: var(--un-box-shadow-small);
	color: var(--un-color-black-5);
	font: var(--un-font-large);
	min-width: 5em;
	outline: 0;
	padding: 0.5em 1em;
}

button[primary] {
	background-color: var(--un-color-accent);
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
