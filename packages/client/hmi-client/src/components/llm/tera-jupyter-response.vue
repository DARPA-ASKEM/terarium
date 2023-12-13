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
						<!-- Show eye icon if the message has a related query -->
						<span class="show-hide-thought" v-if="msg.query" @click="showHideThought">
							<i class="pi pi-eye thought-icon"></i>
							Show/Hide Thought
						</span>
						{{ msg.timestamp }}
					</div>
					<div v-if="props.isExecutingCode">Executing....</div>
				</section>
				<div v-for="m in msg.messages" :key="m.header.msg_id">
					<!-- Handle llm_response type -->
					<div v-if="m.header.msg_type === 'llm_response' && m.content['name'] === 'response_text'">
						<div style="padding-top: 1rem">
							<h5>Agent's response:</h5>
							<div class="llm-response">{{ m.content['text'] }}</div>
						</div>
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
							:context_info="{ id: props.assetId }"
						/>
					</div>

					<!-- Show dataset preview if available -->
					<Accordion
						:active-index="props.autoExpandPreview ? 0 : -1"
						v-if="
							m.header.msg_type === 'dataset' ||
							(m.header.msg_type === 'model_preview' && m.content.data['image/png'])
						"
					>
						<AccordionTab header="Preview (click to collapse/expand)">
							<div>
								Dataset to preview:
								<Dropdown
									v-model="selectedPreviewDataset"
									:options="Object.keys(m.content).map(String)"
									@change="previewSelected"
								/>
							</div>
							<tera-dataset-datatable
								v-if="m.header.msg_type === 'dataset'"
								class="tera-dataset-datatable"
								paginatorPosition="bottom"
								:rows="10"
								:raw-content="m.content[selectedPreviewDataset || 'df'] as CsvAsset"
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
						</AccordionTab>
					</Accordion>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import TeraBeakerCodeCell from '@/components/llm/tera-beaker-response-code-cell.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-beaker-response-thought.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { ref, computed, onMounted, watch } from 'vue';
import { CsvAsset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';

const emit = defineEmits(['cell-updated', 'preview-selected', 'update-kernel-state']);

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
}>();

const codeCell = ref(null);
const resp = ref(<HTMLElement | null>null);
const selectedPreviewDataset = ref(props.defaultPreview);
// Reference for showThought, initially set to false
const showThought = ref(false);

// Computed values for the labels and icons
const showThoughtLabel = computed(() => (showThought.value ? 'Hide reasoning' : 'Show Reasoning'));
const showHideIcon = computed(() =>
	showThought.value ? 'pi pi-fw pi-eye-slash' : 'pi pi-fw pi-eye'
);

const showHideThought = () => {
	showThought.value = !showThought.value;
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

const previewSelected = () => {
	emit('preview-selected', selectedPreviewDataset.value);
};

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
		emit('cell-updated', resp.value, props.msg);
	}
);

defineExpose({
	codeCell
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
	padding-top: 0.7rem;
	white-space: pre-wrap;
	color: black;
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
