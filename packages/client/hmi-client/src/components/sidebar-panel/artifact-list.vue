<script setup lang="ts">
import IconClose32 from '@carbon/icons-vue/es/close/16';

export interface ArtifactListItem {
	id: string | number;
	name: string;
}

const props = defineProps<{
	artifacts: ArtifactListItem[];
	selectedArtifactIds?: string[];
}>();

const emit = defineEmits<{
	(e: 'artifact-clicked', id: string | number): void;
	(e: 'remove-artifact', id: string | number): void;
}>();

function isArtifactSelected(artifactId) {
	if (props.selectedArtifactIds) {
		return props.selectedArtifactIds.includes(artifactId.toString());
	}
	return false;
}
</script>

<template>
	<ul class="artifact-list-container">
		<li
			v-for="artifact in artifacts"
			:key="artifact.id"
			class="row"
			:class="{ active: isArtifactSelected(artifact.id) }"
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

.active .remove-button {
	display: block;
}

.remove-button {
	display: none;
	border-radius: 8px;
	margin-right: 0.5rem;
}

.row:hover .remove-button {
	display: block;
}

.remove-button:hover {
	background-color: var(--un-color-black-20);
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
