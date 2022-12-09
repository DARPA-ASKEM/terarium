<script setup lang="ts">
import IconClose32 from '@carbon/icons-vue/es/close/32';

export interface ArtifactListItem {
	id: string | number;
	name: string;
}

defineProps<{
	artifacts: ArtifactListItem[];
	selectedArtifactId?: string | number;
}>();

const emit = defineEmits<{
	(e: 'artifact-clicked', id: string | number): void;
	(e: 'remove-artifact', id: string | number): void;
}>();
</script>

<template>
	<ul class="artifact-list-container">
		<li
			v-for="artifact in artifacts"
			:key="artifact.id"
			class="row"
			:class="{ active: artifact.id === selectedArtifactId }"
			@click="emit('artifact-clicked', artifact.id)"
		>
			<span>
				{{ artifact.name }}
			</span>
			<IconClose32 class="remove-button" @click.stop="emit('remove-artifact', artifact.id)" />
		</li>
	</ul>
</template>

<style scoped>
.artifact-list-container {
	overflow-y: auto;
}

.row {
	cursor: pointer;
	display: flex;
	flex-direction: row;
	align-items: center;
	border-top: 1px solid var(--separator);
}

.row:first-child {
	border-top: none;
}

.row:hover:not(.active) {
	background-color: var(--un-color-body-surface-secondary);
}

.active {
	background-color: var(--un-color-accent-lighter);
}

.remove-button {
	color: var(--text-color-light);
	display: none;
	height: 2rem;
	width: 2rem;
}

.row:hover .remove-button {
	display: block;
}

.remove-button:hover {
	color: var(--un-font-body);
}

span {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
	display: inline;
	margin: 0.5rem;
	flex: 1;
	min-width: 0;
}
</style>
