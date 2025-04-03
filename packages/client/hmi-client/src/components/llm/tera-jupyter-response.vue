<template>
	<section class="jupyter-response">
		<section>
			<section class="menu-container">
				<!-- Button to show chat window menu -->
				<Button
					v-if="query && !isEditingQuery"
					icon="pi pi-ellipsis-v tool"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click.stop="showChatWindowMenu"
				/>
				<Menu ref="chatWindowMenu" :model="chatWindowMenuItems" :popup="true" />
			</section>
			<section ref="resp" class="resp">
				<section>
					<section>
						<div class="query edit-query-box" v-if="isEditingQuery">
							<Textarea
								v-focus
								v-model="query"
								autoResize
								rows="1"
								@click.stop
								@keydown.enter.prevent="saveEditingQuery"
								@keydown.esc.prevent="cancelEditingQuery"
								style="padding: 8px"
							/>
							<div class="btn-group">
								<Button icon="pi pi-times" rounded text @click="cancelEditingQuery" />
								<Button icon="pi pi-play" rounded text @click="saveEditingQuery" />
							</div>
						</div>
						<div v-else-if="!isEmpty(query)" class="query pb-0">{{ query }}</div>
					</section>
					<!-- TODO: This processing notification was applied to all messages, not just the one that is processing. Need to add id check. -->
					<div v-if="props.isExecutingCode" class="executing-message">
						<span class="pi pi-spinner pi-spin" />Processing
					</div>
				</section>

				<!-- Loop through the messages and display them -->
				<div v-for="m in msg.messages" :key="m.header.msg_id">
					<!-- Handle llm_thought type -->
					<div v-if="m.header.msg_type === 'llm_thought'" class="llm-thought">
						<tera-jupyter-response-thought
							:show-thought="showThought || props.showChatThoughts"
							@toggleThought="showThought = $event"
						>
							<ul>
								<li v-for="(line, index) in formattedLlmThoughtPoints(m.content)" :key="index">
									{{ line }}
								</li>
							</ul>
						</tera-jupyter-response-thought>
						<div class="llm-menu-items">
							<Button
								link
								:label="`${showThought ? 'Hide' : 'Show'} thoughts`"
								@click="() => (showThought = !showThought)"
							/>
							<Button link label="Edit prompt" @click="() => (isEditingQuery = true)" />
						</div>
					</div>
					<!-- Handle llm_response type -->
					<div v-else-if="m.header.msg_type === 'llm_response' && m.content['name'] === 'response_text'">
						<div class="llm-response">{{ m.content['text'] }}</div>
					</div>
					<!-- Handle stream type for stderr -->
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stderr'">
						<div class="error">{{ m.content['text'] }}</div>
					</div>
					<!-- Handle code_cell type -->
					<div v-else-if="m.header.msg_type === 'code_cell'" class="code-cell">
						<tera-beaker-code-cell
							ref="codeCell"
							:jupyter-session="jupyterSession"
							:notebook-item="msg"
							:jupyter-message="m"
							:lang="language"
							:index="index"
							@deleteRequested="onDeleteRequested(m.header.msg_id)"
						/>
					</div>
					<div
						class="flex flex-column"
						v-else-if="['stream', 'display_data', 'execute_result', 'error'].includes(m.header.msg_type)"
					>
						<tera-beaker-code-cell-output ref="codeOutputCell" :jupyter-message="m" />
						<aside class="ml-auto">
							<label class="px-2">Display on node thumbnail</label>
							<Checkbox :model-value="msg.selected" @change="emit('on-selected', $event)" binary />
						</aside>
					</div>
				</div>
			</section>
		</section>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { INotebookItem } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import TeraBeakerCodeCell from '@/components/llm/tera-beaker-code-cell.vue';
import TeraBeakerCodeCellOutput from '@/components/llm/tera-beaker-code-cell-output.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-beaker-response-thought.vue';
import Button from 'primevue/button';
import Textarea from 'primevue/textarea';
import Menu from 'primevue/menu';
import { defineEmits, ref, computed, onMounted } from 'vue';
import type { MenuItem } from 'primevue/menuitem';
import Checkbox from 'primevue/checkbox';

const emit = defineEmits([
	'cell-updated',
	'preview-selected',
	'update-kernel-state',
	'edit-prompt',
	're-run-prompt',
	'delete-prompt',
	'delete-message',
	'on-selected'
]);

const props = defineProps<{
	jupyterSession: SessionContext;
	msg: INotebookItem;
	showChatThoughts: boolean;
	isExecutingCode: boolean;
	language: string;
	assetId?: string;
	autoExpandPreview?: boolean;
	defaultPreview?: string;
	index: Number; // Index of the cell in the notebookItems list
}>();

const codeCell = ref(null);
const codeOutputCell = ref(null);
const resp = ref(<HTMLElement | null>null);
// Reference for showThought, initially set to false
const showThought = ref(false);

const query = ref('');
const isEditingQuery = ref(false);

// Computed values for the labels and icons
const showThoughtLabel = computed(() => (showThought.value ? 'Hide thoughts' : 'Show thoughts'));
const showHideIcon = computed(() => (showThought.value ? 'pi pi-fw pi-eye-slash' : 'pi pi-fw pi-eye'));

// Reference for the chat window menu and its items
const chatWindowMenu = ref();
const chatWindowMenuItems = ref([
	{
		label: 'Edit prompt',
		command: () => {
			isEditingQuery.value = true;
		}
	},
	{ label: 'Re-run answer', command: () => emit('re-run-prompt', props.msg.query_id) },
	{
		label: showThoughtLabel,
		icon: showHideIcon,
		command: () => {
			showThought.value = !showThought.value;
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash',
		command: () => emit('delete-prompt', props.msg.query_id)
	}
] as MenuItem[]);

const saveEditingQuery = () => {
	emit('edit-prompt', props.msg.query_id, query.value);
	isEditingQuery.value = false;
};

const cancelEditingQuery = () => {
	query.value = props.msg.query ?? '';
	isEditingQuery.value = false;
};

// show the chat window menu
const showChatWindowMenu = (event: Event) => chatWindowMenu.value.toggle(event);

const formattedLlmThoughtPoints = (input: { [key: string]: string }) => {
	let thought = '';
	const details: string[] = [];
	// make pretty text for the thought
	Object.keys(input).forEach((key) => {
		if (key === 'thought') {
			thought = input[key];
		} else {
			details.push(`${toTitleCase(key)}: ${input[key]}`);
		}
	});
	return [thought, ...details];
};

// Function to convert a string to Title Case
function toTitleCase(str: string): string {
	return str
		.replace(/_/g, ' ')
		.toLowerCase()
		.split(' ')
		.map((word) => word.charAt(0).toUpperCase() + word.substring(1))
		.join(' ');
}

onMounted(() => {
	query.value = props.msg.query ?? '';
});

defineExpose({
	codeCell,
	codeOutputCell
});

function onDeleteRequested(msgId: string) {
	// Emit an event to request the deletion of a message with the specified msgId
	emit('delete-message', msgId);
}

// // This computed value filters the messages to only include the ones we want to display
// const filteredMessages = computed(() => props.msg.messages.filter(m =>
//   (m.header.msg_type === 'llm_response' && m.content.name === 'response_text') ||
//   (m.header.msg_type === 'stream' && m.content.name === 'stderr') ||
//   (m.header.msg_type === 'stream' && m.content.name === 'stdout') ||
//   (m.header.msg_type === 'code_cell')
// ));
</script>

<style scoped>
.query {
	font-size: var(--font-body-medium);
	font-weight: 600;
	font-family: var(--font-family);
	padding-bottom: var(--gap-4);
}
.edit-query-box {
	display: flex;
	flex-direction: row;
	gap: var(--gap-2);
	align-items: center;
	textarea {
		flex-grow: 1;
	}
}

.executing-message {
	display: none;
	color: var(--text-color-subdued);
	font-size: var(--font-body-small);
	display: flex;
	align-items: center;
	gap: 10px;
	margin-top: var(--gap-2);
}

.error {
	background: var(--red-50);
	color: darkred;
	white-space: pre-wrap;
	padding: var(--gap-4);
}

.jupyter-response {
	position: relative;
	margin: var(--gap-4);
	padding: var(--gap-4);
	display: flex;
	flex-direction: column;
	font-family: var(--font-family);
	border-radius: var(--border-radius);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border-light);
}

.jupyter-response:hover:not(.selected) {
	border: 1px solid color-mix(in srgb, var(--primary-color) 20%, var(--surface-border-light) 80%);
}

.jupyter-response .menu-container {
	display: block;
	position: absolute;
	top: 5px;
	right: 10px;
}

/* Only show Processing message if the cell is selected */
.jupyter-response:not(.selected) .executing-message {
	display: none;
}

.query,
.llm-thought,
.llm-response {
	padding-bottom: var(--gap-2);
}

.llm-thought {
	ul {
		list-style: inside;
	}
	li:first-child {
		list-style: none;
		background-image: url('@assets/svg/icons/magic.svg');
		background-repeat: no-repeat;
		background-size: 1.5rem 1.5rem;
		background-position: 0 0rem;
		padding: 0rem 0 0.625rem 2.25rem;
	}
	li {
		margin-bottom: var(--gap-1);
		padding-left: 2.25rem;
	}
	li::first-letter {
		text-transform: uppercase;
	}
}
.llm-menu-items {
	button {
		padding-left: 0;
		padding-right: 0;
		margin-right: 2rem;
	}
	button:hover :deep(.p-button-label) {
		text-decoration: none;
	}
}
</style>
