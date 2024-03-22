<template>
	<section class="jupyter-response">
		<section>
			<section class="menu-container">
				<!-- Button to show chat window menu -->
				<Button
					v-if="msg.query"
					icon="pi pi-ellipsis-v tool"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click.stop="showChatWindowMenu"
				/>
				<Menu ref="chatWindowMenu" :model="chatWindowMenuItems" :popup="true" />
			</section>
			<section ref="resp" class="resp">
				<section>
					<div v-if="msg.query" class="query">{{ msg.query }}</div>
					<!-- TODO: This processing notification was applied to all messages, not just the one that is processing. Need to add id check. -->
					<!-- <div v-if="props.isExecutingCode" class="executing-message">
						<span class="pi pi-spinner pi-spin"></span>Processing
					</div> -->
				</section>

				<!-- Loop through the messages and display them -->
				<div v-for="m in msg.messages" :key="m.header.msg_id">
					<!-- Handle llm_response type -->
					<div v-if="m.header.msg_type === 'llm_response' && m.content['name'] === 'response_text'">
						<div class="llm-response">{{ m.content['text'] }}</div>
					</div>
					<!-- Handle stream type for stderr -->
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stderr'">
						<div class="error">{{ m.content['text'] }}</div>
					</div>
					<!-- Handle stream type for stdout -->
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stdout'">
						<tera-jupyter-response-thought
							:thought="formattedThought(m.content['text'].trim())"
							:show-thought="showThought || props.showChatThoughts"
						/>
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
					</div>
				</div>
			</section>
		</section>
	</section>
</template>

<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import TeraBeakerCodeCell from '@/components/llm/tera-beaker-response-code-cell.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-beaker-response-thought.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { defineEmits, ref, computed, onMounted, watch } from 'vue';
import type { CsvAsset } from '@/types/Types';

const emit = defineEmits([
	'cell-updated',
	'deleteMessage',
	'preview-selected',
	'update-kernel-state'
]);

const props = defineProps<{
	jupyterSession: SessionContext;
	msg: {
		query_id: string;
		query: string | null;
		timestamp: string;
		messages: JupyterMessage[];
		resultingCsv: CsvAsset | null;
	};
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

// Computed values for the labels and icons
const showThoughtLabel = computed(() => (showThought.value ? 'Hide reasoning' : 'Show reasoning'));
const showHideIcon = computed(() =>
	showThought.value ? 'pi pi-fw pi-eye-slash' : 'pi pi-fw pi-eye'
);

// Reference for the chat window menu and its items
const chatWindowMenu = ref();
const chatWindowMenuItems = ref([
	{ label: 'Edit prompt', command: () => console.log('Edit prompt') },
	{ label: 'Re-run answer', command: () => console.log('Re-run prompt') },
	{
		label: showThoughtLabel,
		icon: showHideIcon,
		command: () => {
			showThought.value = !showThought.value;
		}
	},
	{ label: 'Delete', icon: 'pi pi-fw pi-trash', command: () => console.log('Delete prompt') }
]);

// show the chat window menu
const showChatWindowMenu = (event: Event) => chatWindowMenu.value.toggle(event);

// format the thought text
const formattedThought = (input: string) => {
	const lines = input.split('\n'); // Split the string into lines
	const formattedLines = lines.map((line) => {
		const index = line.indexOf(':');
		if (index > -1) {
			// Transform the category to title case and remove underscores
			const category = toTitleCase(line.slice(0, index));
			// Add a newline character after the colon
			return `${category}:\n${line.slice(index + 2)}`;
		}
		return line;
	});
	return formattedLines.join('\n\n'); // Combine the formatted lines into a single string with an extra newline between each
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

function onDeleteRequested(msgId) {
	// Emit an event to request the deletion of a message with the specified msgId
	emit('deleteMessage', msgId);
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
	padding-bottom: var(--gap);
	padding-left: 60px;
	/* Add ai-assistant icon */
	background-image: url('@assets/svg/icons/message.svg');
	background-repeat: no-repeat;
	background-position: 4px 3px;
}
.executing-message {
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

.jupyter-response:hover {
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

.llm-response {
	padding-left: 60px;
	padding-right: 2rem;
	padding-bottom: var(--gap-small);
	white-space: pre-wrap;
	color: var(--text-color);
	/* Add ai-assistant magic icon */
	background-image: url('@assets/svg/icons/magic.svg');
	background-repeat: no-repeat;
	background-position: 4px 2px;
}

.date {
	font-family: var(--font-family);
}

.show-hide-thought {
	font-size: small;
	color: gray;
}

.thought-icon {
	padding: 5px;
}
</style>
