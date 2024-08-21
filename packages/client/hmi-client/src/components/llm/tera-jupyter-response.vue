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
							/>
							<div class="btn-group">
								<Button icon="pi pi-times" rounded text @click="cancelEditingQuery" />
								<Button icon="pi pi-play" rounded text @click="saveEditingQuery" />
							</div>
						</div>
						<div v-else-if="!isEmpty(query)" class="query">{{ query }}</div>
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
						<tera-jupyter-response-thought :show-thought="showThought || props.showChatThoughts">
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
							<Button link label="Edit the prompts" @click="() => (isEditingQuery = true)" />
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
							:language="m.content['language']"
							:code="m.content['code']"
							:autorun="true"
							:notebook-item-id="msg.query_id"
							context="dataset"
							:context_info="{ id: props.assetId, query: msg.query }"
							@deleteRequested="onDeleteRequested(m.header.msg_id)"
						/>

						<tera-beaker-code-cell-2
							:jupyter-session="jupyterSession"
							:language="m.content['language']"
							:code="m.content['code']"
							:jupyter-message="m"
						/>
					</div>
					<div v-else-if="['stream', 'display_data', 'execute_result', 'error'].includes(m.header.msg_type)">
						<tera-beaker-code-cell-output :jupyter-message="m" />
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
import TeraBeakerCodeCell from '@/components/llm/tera-beaker-response-code-cell.vue';
import TeraBeakerCodeCell2 from '@/components/llm/tera-beaker-code-cell.vue';
import TeraBeakerCodeCellOutput from '@/components/llm/tera-beaker-code-cell-output.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-beaker-response-thought.vue';
import Button from 'primevue/button';
import Textarea from 'primevue/textarea';
import Menu from 'primevue/menu';
import { defineEmits, ref, computed, onMounted, watch } from 'vue';

const emit = defineEmits([
	'cell-updated',
	'preview-selected',
	'update-kernel-state',
	'edit-prompt',
	're-run-prompt',
	'delete-prompt',
	'delete-message'
]);

const props = defineProps<{
	jupyterSession: SessionContext;
	msg: INotebookItem;
	showChatThoughts: boolean;
	isExecutingCode: boolean;
	assetId?: string;
	autoExpandPreview?: boolean;
	defaultPreview?: string;
	index: Number; // Index of the cell in the notebookItems list
}>();

const codeCell = ref(null);
const resp = ref(<HTMLElement | null>null);
// Reference for showThought, initially set to false
const showThought = ref(false);

const query = ref('');
const isEditingQuery = ref(false);

// Computed values for the labels and icons
const showThoughtLabel = computed(() => (showThought.value ? 'Hide reasoning' : 'Show reasoning'));
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
]);

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
	emit('cell-updated', resp.value, props.msg);
});

watch(
	() => props.msg.messages,
	() => {
		emit('cell-updated', resp.value, props.msg, 'delete-cell');
	}
);

defineExpose({
	codeCell
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
	/* Add ai-assistant icon */
	background-image: url('@assets/svg/icons/message.svg');
	background-repeat: no-repeat;
	background-position: 4px 3px;
}
.edit-query-box {
	display: flex;
	flex-direction: row;
	padding-bottom: 2px;
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
	margin-top: var(--gap-small);
}

.error {
	color: darkred;
	white-space: pre-wrap;
	padding-top: 10px;
}

.jupyter-response {
	position: relative;
	margin: var(--gap);
	padding: var(--gap-small);
	display: flex;
	flex-direction: column;
	font-family: var(--font-family);
	border-radius: var(--border-radius);
	margin-top: 10px;
	background-color: var(--surface-0);
	transition:
		background-color 0.3s,
		border 0.3s;
	border: 1px solid rgba(0, 0, 0, 0);
}

.jupyter-response:hover:not(.selected) {
	background-color: var(--surface-50);
	border: 1px solid var(--surface-border-light);
}

.jupyter-response .menu-container {
	display: none;
	position: absolute;
	top: 5px;
	right: 10px;
}

.jupyter-response:hover .menu-container {
	display: block;
	position: absolute;
	top: 5px;
	right: 10px;
}

.query,
.llm-thought,
.llm-response {
	padding-left: 57px;
	padding-right: 2rem;
	padding-bottom: var(--gap-small);
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
		background-position: 0 0.625rem;
		padding: 0.625rem 0 0.625rem 1.875rem;
	}
	li {
		margin-bottom: var(--gap-xsmall);
		padding-left: 0.625rem;
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

.llm-response {
	white-space: pre-wrap;
	color: var(--text-color);
	/* Add ai-assistant magic icon */
	background-image: url('@assets/svg/icons/magic.svg');
	background-repeat: no-repeat;
	background-position: 4px 2px;
}
</style>
