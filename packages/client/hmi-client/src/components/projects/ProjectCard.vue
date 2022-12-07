<script setup lang="ts">
export interface Props {
	name: string;
}
const props = withDefaults(defineProps<Props>(), {
	name: 'Default Project Name'
});

const placeholderRandomColorOpacity = Math.random();
</script>

<template>
	<div class="project-card">
		<div class="placeholder-color">
			<div class="placeholder-color-random" :style="{ opacity: placeholderRandomColorOpacity }" />
		</div>
		<footer>{{ props?.name }}</footer>
	</div>
</template>

<style scoped>
.project-card {
	border: 1px solid var(--un-color-body-stroke);
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	height: 15rem;
	min-width: 20rem;
	border-radius: 0.5rem;
	text-align: left;
	cursor: pointer;
	/* Round the image/placeholder color's corners by clipping them to fit the card */
	overflow: hidden;
}

footer {
	border-top: 1px solid var(--un-color-body-stroke);
	padding: 0.5rem 1rem;
}

.placeholder-color {
	flex: 1;
	min-height: 0;
	background: var(--un-color-accent-lighter);
	position: relative;
}

.placeholder-color-random {
	background: var(--un-color-accent-light);
	position: absolute;
	left: 0;
	top: 0;
	width: 100%;
	height: 100%;
}
</style>
