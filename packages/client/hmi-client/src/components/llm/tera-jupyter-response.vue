<template>
	<div>
		<section class="jupyter-response">
			<div class="menu-container">
				<!-- Button to show chat window menu -->
				<Button
					v-if="msg.query"
					icon="pi pi-ellipsis-v tool"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click.stop="showChatWindowMenu"
				/>
				<Menu ref="chatWindowMenu" :model="chatWindowMenuItems" :popup="true" />
			</div>
			<div ref="resp" class="resp">
				<section class="query-title">
					<div class="query">{{ msg.query }}</div>
					<div class="date">
						<span>
							<!-- Show eye icon if the message has been drawn and there's a query -->
							<i
								v-if="props.hasBeenDrawn && msg.query"
								class="pi pi-eye thought-icon"
								v-tooltip="`Show/Hide Thought`"
								@click="showHideThought"
							></i>
							<!-- Show spinning icon if the message is still being drawn -->
							<i v-else-if="!props.hasBeenDrawn" class="pi pi-spin pi-spinner thought-icon"></i>
						</span>
						{{ msg.timestamp }}
					</div>
					<div v-if="props.isExecutingCode">Executing....</div>
				</section>
				<div v-for="(m, index) in msg.messages" :key="index">
					<!-- Handle llm_response type -->
					<div v-if="m.header.msg_type === 'llm_response' && m.content['name'] === 'response_text'">
						<div class="llm-response">{{ m.content['chatty_response'] }}</div>
					</div>
					<!-- Handle stream type for stderr -->
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stderr'">
						<div class="error">{{ m.content['text'] }}</div>
					</div>
					<!-- Handle stream type for stdout -->
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stdout'">
						<tera-jupyter-response-thought
							:thought="formattedThought(m.content['text'].trim())"
							:has-been-drawn="props.hasBeenDrawn"
							:show-thought="!msg.query || showThought || props.showChatThoughts"
							@has-been-drawn="thoughtHasBeenDrawn"
							@is-typing="emit('is-typing')"
						/>
					</div>
					<!-- Handle code_cell type -->
					<div v-else-if="m.header.msg_type === 'code_cell'" class="code-cell">
						<tera-chatty-code-cell
							:jupyter-session="jupyterSession"
							:language="m.content['language']"
							:code="m.content['code']"
							:autorun="true"
							:notebook-item-id="msg.query_id"
							context="dataset"
							:context_info="{ id: props.assetId }"
						/>
					</div>

					<!-- Show dataset preview if available -->
					<tera-dataset-datatable
						v-else-if="m.header.msg_type === 'dataset'"
						class="tera-dataset-datatable"
						paginatorPosition="bottom"
						:rows="10"
						:raw-content="(m.content as CsvAsset)"
						:preview-mode="true"
						:showGridlines="true"
						table-style="width: 100%; font-size: small;"
					/>
					<!-- Show preview image if available -->
					<img
						v-else-if="m.header.msg_type === 'model_preview' && m.content.data['image/png']"
						:src="`data:image/png;base64,${m.content.data['image/png']}`"
						alt="Preview of model network graph"
					/>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import TeraChattyCodeCell from '@/components/llm/tera-chatty-response-code-cell.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-chatty-response-thought.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { ref, computed, onUpdated, onMounted } from 'vue';
import { CsvAsset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';

const emit = defineEmits(['has-been-drawn', 'is-typing', 'cell-updated']);

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
	hasBeenDrawn: boolean;
	isExecutingCode: boolean;
	assetId?: string;
}>();

const resp = ref(<HTMLElement | null>null);
// Reference for showThought, initially set to false
const showThought = ref(false);

// Computed values for the labels and icons
const showThoughtLabel = computed(() => (showThought.value ? 'Hide reasoning' : 'Show Reasoning'));
const showHideIcon = computed(() =>
	showThought.value ? 'pi pi-fw pi-eye-slash' : 'pi pi-fw pi-eye'
);

const showHideThought = () => {
	showThought.value = !showThought.value;
	resp.value?.scrollTo();
};

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

// emit when the thought has been drawn
const thoughtHasBeenDrawn = () => {
	emit('has-been-drawn');
};

onMounted(() => {
	emit('cell-updated', resp.value, props.msg);
});
onUpdated(() => {
	emit('cell-updated', resp.value, props.msg);
});
</script>

<style scoped>
.query {
	font-size: 24px;
	font-family: var(--font-family);
	padding-bottom: 5px;
}

.error {
	color: darkred;
	white-space: pre-wrap;
	padding-top: 10px;
}

.jupyter-response {
	position: relative;
	padding: 5px;
	display: flex;
	flex-direction: column;
	background-color: var(--gray-100);
	font-family: var(--font-family);
	border-radius: 3px;
	margin-top: 10px;
	transition: background-color 0.3s, border 0.3s;
	border: 1px solid rgba(0, 0, 0, 0);
}

.jupyter-response:hover {
	background-color: var(--gray-300);
	border: 1px solid var(--gray-800);
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
	color: green;
}

.date {
	font-family: var(--font-family);
}

.thought-icon {
	padding: 5px;
}
</style>
