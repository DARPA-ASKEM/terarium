<template>
	<div style="flex-direction: column; overflow-y: scroll">
		{{ assetName }}
		{{ assetType }}
		<div style="flex-direction: row; flex: 1; display: flex">
			<div style="display: flex; flex-direction: column; flex: 1">
				<chatty-input
					:llm-context="jupyterSession"
					placeholder-message="How can I help you today?"
					@new-message="newMessage"
					context="dataset"
					:context_info="{ id: props.assetId !== null ? props.assetId : 13 }"
				/>
				<div id="chatty-history">
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
							<jupyter-code-cell
								:jupyter-session="jupyterSession"
								:language="message.content['language']"
								:code="message.content['code']"
								:autorun="true"
								context="dataset"
								:context_info="{ id: props.assetId !== null ? props.assetId : 13 }"
							/>
						</div>
						<div v-else>Other: {{ message }}</div>
						<hr />
					</div>
				</div>
			</div>
		</div>
		<div v-if="datasetPreview">
			<JupyterDataPreview :jupyter-session="jupyterSession" :raw-content="datasetPreview" />
		</div>
	</div>
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import ChattyInput from '@/components/widgets/chatty-input.vue';
import JupyterCodeCell from '@/components/widgets/jupyter-code-cell.vue';
import JupyterDataPreview from '@/components/widgets/jupyter-dataset-preview.vue';
import { ref, watch } from 'vue';

import { newSession, JupyterMessage } from '@/utils/jupyter';
const jupyterSession = newSession('llmkernel', 'ChattyNode');

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
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
	if (
		['stream', 'code_cell', 'llm_request', 'chatty_response'].indexOf(event.header.msg_type) > -1
	) {
		messages.value.push(event);
	} else if (event.header.msg_type == 'dataset') {
		datasetPreview.value = event.content;
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
	height: auto;
}

#chatty-input {
	flex-grow: 1;
	height: fit-content;
}

#chatty-history {
	height: auto;
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
