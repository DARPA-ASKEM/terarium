<script setup lang="ts">
import IconNoImage32 from '@carbon/icons-vue/es/no-image/32';
import IconList24 from '@carbon/icons-vue/es/list/24';

interface Props {
	name: string;
}
const props = withDefaults(defineProps<Props>(), { name: '' });
</script>

<template>
	<li class="project-card">
		<IconNoImage32 />
		<div class="details">
			<div class="name">{{ props?.name }}</div>
			<div class="actions">
				<IconList24 />
			</div>
		</div>
	</li>
</template>

<style scoped>
.project-card {
	color: var(--un-color-black-80);
	border: 1px solid var(--un-color-black-20);
	background-color: var(--un-color-black-5);
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

.details {
	border-top: 1px solid var(--un-color-black-20);
}

.details div {
	padding: 0.5rem 1rem;
}

.details .actions {
	display: none;
	padding: 0 0 0.25rem 1rem;
}

.project-card:hover .details .actions {
	display: block;
}

svg {
	color: var(--un-color-black-40);
	cursor: pointer;
	margin: auto;
}

svg:hover {
	color: var(--un-color-black-30);
}
</style>
