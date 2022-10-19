<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';

function createNewProject() {}
</script>

<template>
	<div class="new-project-card">
		<Button @onclick="createNewProject"
			>Create New Project
			<IconAddFilled32 />
		</Button>
	</div>
</template>

<style scoped>
.new-project-card {
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

.new-project-card button {
	font-size: 1.25rem;
	font-weight: 500;
	border-radius: 0.5rem;
	width: 85%;
	margin: auto;
	justify-content: center;
	cursor: pointer;
	background-color: transparent;
	color: var(--un-color-body-text-disabled);
	box-shadow: none;
}

.new-project-card button:hover {
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-body-text-secondary);
}

.new-project-card button svg {
	color: var(--un-color-body-text-disabled);
}

.new-project-card button:hover svg {
	color: var(--un-color-body-text-secondary);
}
</style>
