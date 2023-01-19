<script setup lang="ts">
export interface Props {
	name: string;
}
const props = withDefaults(defineProps<Props>(), {
	name: 'Default Project Name'
});
</script>

<template>
	<div class="project-card">
		<div class="placeholder-color" />
		<footer>{{ props?.name }}</footer>
	</div>
</template>

<style scoped>
.project-card {
	border: 1px solid var(--surface-border);
	background-color: var(--surface-section);
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
	border-top: 1px solid var(--surface-border);
	padding: 0.5rem 1rem;
}

.placeholder-color {
	flex: 1;
	min-height: 0;
	background: var(--gray-200);
	position: relative;
}
</style>
