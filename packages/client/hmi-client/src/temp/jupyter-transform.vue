<template>
	<div>
		{{ assetName }}
		{{ assetType }}
		<div>
			<div>
				<tera-jupyter-chat
					:llm-context="jupyterSession"
					:project="props.project"
					placeholder-message="What do you want to do?"
					@new-message="newMessage"
					context="dataset"
					:context_info="{
						id: props.assetId !== undefined ? props.assetId : 'f664c2bf-4c6a-4948-810c-0686e555de83'
					}"
				/>
				<div v-if="showHistory" id="chatty-history">
					<div v-for="message in messages" :key="message.header.msg_id">
						<div v-if="message.header.msg_type === 'llm_request'" class="query">
							Query: {{ message.content['request'] }}
						</div>
						<div
							v-else-if="
								message.header.msg_type === 'llm_response' &&
								message.content['name'] === 'response_text'
							"
							class="answer"
						>
							Response: {{ message.content['text'] }}
						</div>
						<div
							v-else-if="
								message.header.msg_type === 'stream' && message.content['name'] === 'stdout'
							"
							class="thought"
						>
							{{ message.content['text'] }}
						</div>
						<div
							v-else-if="
								message.header.msg_type === 'stream' && message.content['name'] === 'stderr'
							"
							class="error"
						>
							Error: {{ message.content['text'] }}
						</div>
						<div v-else-if="message.header.msg_type === 'code_cell'" class="code-cell">
							<tera-chatty-code-cell
								:jupyter-session="jupyterSession"
								:language="message.content['language']"
								:code="message.content['code']"
								:autorun="true"
								context="dataset"
								:context_info="{
									id:
										props.assetId !== undefined
											? props.assetId
											: 'f664c2bf-4c6a-4948-810c-0686e555de83'
								}"
							/>
						</div>
						<div v-else>Other: {{ message }}</div>
						<hr />
					</div>
				</div>
			</div>
		</div>
		<div v-if="props.showHistory && datasetPreview">
			<JupyterDataPreview :jupyter-session="jupyterSession" :raw-content="datasetPreview" />
		</div>
	</div>
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import TeraJupyterChat from '@/components/llm/tera-jupyter-chat.vue';
import TeraChattyCodeCell from '@/components/llm/tera-chatty-response-code-cell.vue';
import JupyterDataPreview from '@/components/llm/jupyter-dataset-preview.vue';
import { ref, watch } from 'vue';

import { newSession, JupyterMessage } from '@/services/jupyter';
const jupyterSession = await newSession('llmkernel', 'ChattyNode');

const emit = defineEmits(['update-data', 'jupyter-event']);

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
	showHistory?: { value: boolean; default: false };
}>();

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);

const messages = ref<JupyterMessage[]>([]);
const datasetPreview = ref(null);

const newMessage = (event) => {
	if (['stream', 'code_cell', 'llm_request', 'llm_response'].indexOf(event.header.msg_type) > -1) {
		if (event.header.msg_type === 'stream' && /tool: final_answer/.test(String(event))) {
			return;
		}
		messages.value.push(event);
		emit('jupyter-event', messages.value);
	} else if (event.header.msg_type == 'dataset') {
		datasetPreview.value = event.content;
		emit('update-data', datasetPreview.value);
	} else {
		console.log('Unknown Jupyter event', event);
	}
};
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
}

.asset {
	padding-top: 1rem;
}

.p-tabmenu:deep(.p-tabmenuitem) {
	display: inline;
	max-width: 15rem;
}

.p-tabmenu:deep(.p-tabmenu-nav .p-tabmenuitem .p-menuitem-link) {
	padding: 1rem;
	text-decoration: none;
}

.p-tabmenu:deep(.p-menuitem-text) {
	height: 1rem;
	display: inline-block;
	overflow: hidden;
	text-overflow: ellipsis;
}

.chatty-container {
	display: flex;
	position: relative;
	flex-grow: 1;
}

#chatty-input {
	flex-grow: 1;
	height: fit-content;
}

#chatty-history {
	max-height: 30vh;
	flex-direction: column;
	flex-basis: auto;
	overflow-y: scroll;
}

.query {
	color: green;
	white-space: pre;
}

.answer {
	color: darkblue;
	white-space: pre-wrap;
}

.thought {
	color: blueviolet;
	white-space: pre-wrap;
}

.error {
	color: darkred;
	white-space: pre-wrap;
}

.code-cell {
	flex-direction: row;
	display: flex;
}
</style>
