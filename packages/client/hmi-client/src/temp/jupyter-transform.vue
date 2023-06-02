<template>
	<div style="flex-direction: column; overflow-y: scroll;">
	{{ assetName }}
	{{ assetType }}
	<div style="flex-direction: row; flex: 1; display: flex;">
		<div style="display: flex; flex-direction: column; flex: 1">
		<chatty-input :llm-context="llmContext" placeholder-message="How can I help you today?" 
					@new-message="newMessage" context="dataset" :context_info="{id: 13}"/>
		<div id="chatty-history">
			<div v-for="message in messages" :key="message.msg_id">
				<div v-if="message.msg_type === 'llm_request'" class="query">
					Query: {{ message.content?.request }}
				</div>
				<div
					v-else-if="
						message.msg_type === 'chatty_response' && message.content.name === 'response_text'
					"
					class="answer"
				>
					Response: {{ message.content?.text }}
				</div>
				<div
					v-else-if="message.msg_type === 'stream' && message.content.name === 'stdout'"
					class="thought"
				>
					{{ message.content?.text }}
				</div>
				<div
					v-else-if="message.msg_type === 'stream' && message.content.name === 'stderr'"
					class="error"
				>
					Error: {{ message.content?.text }}
				</div>
				<div v-else-if="message.msg_type === 'code_cell'" class="code-cell">
					<jupyter-code-cell :language="message.content?.language" :code="message.content?.code" 
									:jupyter-context="pythonContext" :autorun="true" context="dataset" :context_info="{id: 13}"/>
				</div>
				<div v-else>Other: {{ message }}</div>
				<hr />
			</div>
		</div>
	</div>
	</div>
	<div v-if="datasetPreview">
		<JupyterDataPreview :llm-context="pythonContext" :raw-content="datasetPreview"/>
	</div>
</div>
</template>

<script setup lang="ts">
// import SliderPanel from '@/components/widgets/slider-panel.vue';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import ChattyInput from '@/components/widgets/chatty-input.vue';
import JupyterCodeCell from '@/components/widgets/jupyter-code-cell.vue';
import JupyterDataPreview from '@/components/widgets/jupyter-dataset-preview.vue';
import { mimeService } from '@/utils/jupyter';
import { ref, watch, computed } from 'vue';

import { newSession } from '@/utils/jupyter';
const llmContext = newSession('llmkernel', 'ChattyNode');
const pythonContext = llmContext;

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes;
}>();

const messages = ref<Object[]>([
{
  "header": {
    "msg_id": "1c007cea-414bc80e7b287430fcb77ea3_14_20",
    "msg_type": "code_cell",
    "username": "username",
    "session": "1c007cea-414bc80e7b287430fcb77ea3",
    "date": "2023-05-17T19:12:17.525761Z",
    "version": "5.3"
  },
  "msg_id": "1c007cea-414bc80e7b287430fcb77ea3_14_20",
  "msg_type": "code_cell",
  "parent_header": {
    "date": "2023-05-17T19:11:39.841000Z",
    "msg_id": "babbefae-9780-4da4-8ec7-969055b51428",
    "msg_type": "execute_request",
    "session": "c3c4acea-4cde-4309-87c2-87b64524634b",
    "username": "",
    "version": "5.2"
  },
  "metadata": {},
  "content": {
    "language": "python",
    "code": "import matplotlib.pyplot as plt\n\n# Convert timestamp to datetime\ndf['timestamp'] = pd.to_datetime(df['timestamp'])\n\n# Calculate the rate of change of infected\ndf['dI_dt'] = df['I'].diff()\n\n# Plot the rate of change of infected over time\nplt.figure(figsize=(10, 6))\nplt.plot(df['timestamp'], df['dI_dt'])\nplt.xlabel('Time')\nplt.ylabel('Rate of Change of Infected')\nplt.title('Rate of Change of Infected Over Time')\nplt.grid()\nplt.show()"
  },
  "buffers": [],
  "channel": "iopub"
}
]);
const datasetPreview = ref({});

watch(
	() => [
		props.assetId, // Once the route name changes, add/switch to another tab
		props.project
	],
	() => {
		console.log(props.project, props.assetId);
	}
);

const newMessage = (event) => {
	if (["stream", "code_cell", "llm_request", "chatty_response"].indexOf(event.msg_type) > -1) {
		console.log("e", event);
		messages.value.push(event);
	}
	else if (event.msg_type == "dataset") {
		datasetPreview.value = event.content;
	}
	else {
		console.log(event);
	}
}

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
