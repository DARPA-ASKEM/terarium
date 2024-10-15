<template>
	<section class="card-container" :style="{ width: `${cardWidth}px` }">
		<section class="card" ref="cardRef">
			<div class="drag-handle"><i class="pi pi-pause" /></div>
			<main>
				<header>
					<span :class="{ 'edit-name': isEditable }" @click="turnOnNameEdit" v-if="!isEditingName">
						{{ model.header.name }}
					</span>
					<Textarea
						v-else
						ref="nameInputRef"
						v-model="name"
						@keyup.enter="updateName()"
						@keydown.enter="$event.preventDefault()"
						@focusout="updateName"
					/>
				</header>
				<tera-model-diagram :model="model" :feature-config="{ isPreview: true }" />
			</main>
			<Button v-if="isEditable" @click="toggle" icon="pi pi-ellipsis-v" rounded text />
			<Menu ref="menu" :model="cardOptions" :popup="true" />
		</section>
		<ul>
			<li
				v-for="(port, index) in ports"
				:id="id ? `${id}-${port.id}` : undefined"
				class="port"
				:class="{ selectable: isEditable }"
				:key="index"
				@mouseenter="emit('port-mouseover', $event, cardWidth)"
				@mouseleave="emit('port-mouseleave')"
				@focus="() => {}"
				@blur="() => {}"
				@click.stop="emit('port-selected', port.id)"
			>
				{{ port.id }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';
import Button from 'primevue/button';
import Textarea from 'primevue/textarea';
import TeraModelDiagram from '@/components/model/petrinet/tera-model-diagram.vue';
import Menu from 'primevue/menu';

const props = defineProps<{
	model: any;
	isEditable: boolean;
	isDecomposed: boolean;
	id?: string;
	showParameters?: boolean;
}>();

const emit = defineEmits(['port-mouseover', 'port-mouseleave', 'port-selected', 'update-name', 'remove']);

// Used to pass card width.
// Unsure if we want to set widths on certain cards but for now this works
const cardRef = ref();
const nameInputRef = ref();
const isEditingName = ref(false);
const name = ref(props.model.header.name);

const menu = ref();
const cardOptions = ref([
	{
		label: 'Remove',
		icon: 'pi pi-trash',
		command: () => emit('remove')
	}
]);
const toggle = (event) => {
	menu.value.toggle(event);
};

const cardWidth = computed(() => cardRef.value?.clientWidth ?? 0);

const ports = computed(() =>
	props.showParameters
		? [...props.model.model.states, ...props.model.semantics.ode.parameters]
		: props.model.model.states
);

async function turnOnNameEdit() {
	if (props.isEditable && props.isDecomposed) {
		isEditingName.value = true;
		await nextTick();
		if (nameInputRef.value) nameInputRef.value.$el.focus();
	}
}

function updateName() {
	emit('update-name', name.value);
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
	cursor: pointer;
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

			& > .edit-name {
				cursor: pointer;
			}

			& > .p-inputtext {
				padding: 0.2rem 0.3rem;
				text-align: center;
				width: fit-content;
				font-size: var(--font-caption);
			}
		}
	}
	& > .p-button {
		display: none;
		position: absolute;
		bottom: 0;
		right: 0;
	}

	&:hover > .p-button {
		display: block;
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

.drag-handle {
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
