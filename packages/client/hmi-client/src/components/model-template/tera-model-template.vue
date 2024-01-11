<template>
	<section class="card-container">
		<section class="card" ref="cardRef">
			<div class="draggable"><i class="pi pi-pause" /></div>
			<main>
				<header>
					<span
						:class="{ 'edit-name': isEditable }"
						@click="if (isEditable) isEditingName = true;"
						v-if="!isEditingName"
					>
						{{ card?.name }}
					</span>
					<InputText v-else size="small" type="text" v-model="name" @keyup.enter="updateName" />
				</header>
				<section>Diagram/Equations</section>
			</main>
			<Button icon="pi pi-ellipsis-v" rounded text />
		</section>
		<ul>
			<li
				v-for="({ id }, index) in [...model.model.states, ...model.model.transitions]"
				class="port"
				:class="{ selectable: isEditable }"
				:key="index"
				@mouseenter="emit('port-mouseover', $event, cardRef?.clientWidth ?? 0)"
				@mouseleave="emit('port-mouseleave')"
				@focus="() => {}"
				@blur="() => {}"
				@click.stop="emit('port-selected')"
			>
				{{ id }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';

interface ModelTemplate {
	id: number;
	name: string;
	x: number;
	y: number;
}

const props = defineProps<{ model: any; isEditable: boolean }>();

const emit = defineEmits(['port-mouseover', 'port-mouseleave', 'port-selected', 'update-name']);

// Used to pass card width.
// Unsure if we want to set widths on certain cards but for now this works
const cardRef = ref();

const isEditingName = ref(false);

const card = computed<ModelTemplate>(
	() =>
		props.model.metadata.templateCard ?? {
			id: -1,
			name: props.model.header.name,
			x: 0,
			y: 0
		}
);

const name = ref(props.model.header.name);

function updateName() {
	emit('update-name', name);
	isEditingName.value = false;
}
</script>

<style scoped>
.card-container {
	display: flex;
	font-size: var(--font-caption);
}

.card {
	display: flex;
	background-color: var(--surface-section);
	border-radius: var(--border-radius-medium);
	outline: 1px solid var(--surface-border-alt);
	min-width: 12rem;
	position: relative;
	box-shadow:
		0px 1px 3px 0px rgba(0, 0, 0, 0.08),
		0px 1px 2px 0px rgba(0, 0, 0, 0.04);

	& > main {
		width: 100%;
		margin: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;
		overflow: hidden;

		& > header {
			text-align: center;
			height: 2rem;

			& > .edit-name {
				cursor: pointer;
			}

			& > .p-inputtext.p-inputtext-sm {
				padding: 0.2rem 0.3rem;
				text-align: center;
			}
		}

		& > * {
			margin: 0 auto;
		}
	}

	& > .p-button {
		display: none;
		position: absolute;
		bottom: 0;
		right: 0;
	}
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	padding: 0.5rem 0;
	margin: auto 0;

	& > li {
		background-color: var(--surface-section);
		border: 1px solid var(--surface-border-alt);
		border-top-right-radius: var(--border-radius);
		border-bottom-right-radius: var(--border-radius);
		padding: 0.15rem 0.25rem;
		color: var(--text-color-subdued);
		box-shadow:
			0px 1px 3px 0px rgba(0, 0, 0, 0.08),
			0px 1px 2px 0px rgba(0, 0, 0, 0.04);
		/* Font should be "Latin Modern Math" */
		font-family: serif;
		font-style: italic;
	}

	& > li.selectable:hover {
		background-color: var(--surface-highlight);
		cursor: pointer;
	}
}

.draggable {
	width: 0.75rem;
	border-top-left-radius: var(--border-radius-medium);
	border-bottom-left-radius: var(--border-radius-medium);
	background-color: var(--surface-highlight);
	display: flex;
	align-items: center;

	& > .pi {
		font-size: 0.75rem;
		color: var(--text-color-subdued);
	}
}
</style>
