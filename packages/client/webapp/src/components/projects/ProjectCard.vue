<script setup lang="ts">
import IconNoImage32 from '@carbon/icons-vue/es/no-image/32';

interface Props {
	name: string;
}
const props = withDefaults(defineProps<Props>(), {
	name: 'Default Project Name'
});
</script>

<template>
	<div class="project-card">
		<IconNoImage32 />
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
	margin: 0.5rem;
	transition: 0.2s;
	text-align: left;
}

.project-card:hover {
	height: 20rem;
	min-width: 25rem;
	cursor: pointer;
}

footer {
	border-top: 1px solid var(--un-color-body-stroke);
	padding: 0.5rem 1rem;
}

svg {
	color: var(--un-color-body-text-disabled);
	cursor: pointer;
	margin: auto;
}
</style>
