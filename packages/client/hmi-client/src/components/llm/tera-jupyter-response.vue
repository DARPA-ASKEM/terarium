<template>
	<div>
		<section class="jupyter-message">
			<div class="absolute">
				<Button
					v-if="msg.query"
					icon="pi pi-ellipsis-v tool"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click.stop="showChatWindowMenu"
				/>
				<Menu ref="chatWindowMenu" :model="chatWindowMenuItems" :popup="true" />
				<!-- <i class="pi pi-chevron-up tool" /> -->
			</div>
			<div class="resp">
				<section class="query-title">
					<div class="query">{{ msg.query }}</div>
					<div class="date">
						<span
							><i
								span
								v-if="props.hasBeenDrawn && msg.query"
								class="pi pi-eye thought-icon"
								v-tooltip="`Show/Hide Thought`"
								@click="showThought = !showThought"
							></i
							><i
								span
								v-if="!props.hasBeenDrawn"
								class="pi pi-spin pi-spinner thought-icon"
							></i></span
						>{{ msg.timestamp }}
					</div>
				</section>
				<!----------------------------------------------------->
				<div v-for="(m, index) in msg.messages" :key="index">
					<div v-if="m.header.msg_type === 'llm_response' && m.content['name'] === 'response_text'">
						<div class="response">{{ m.content['chatty_response'] }}</div>
					</div>
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stderr'">
						<div class="error">{{ m.content['text'] }}</div>
					</div>
					<div v-else-if="m.header.msg_type === 'stream' && m.content['name'] === 'stdout'">
						<tera-jupyter-response-thought
							:thought="m.content['text'].trim()"
							:has-been-drawn="props.hasBeenDrawn"
							:show-thought="!msg.query || showThought"
							@has-been-drawn="thoughtHasBeenDrawn"
						/>
					</div>
					<div v-else-if="m.header.msg_type === 'code_cell'" class="code-cell">
						<jupyter-code-cell
							:jupyter-session="jupyterSession"
							:language="m.content['language']"
							:code="m.content['code']"
							:autorun="true"
							context="dataset"
							:context_info="{
								id: props.assetId
							}"
						/>
					</div>
				</div>
			</div>
		</section>
	</div>
</template>
<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import JupyterCodeCell from '@/components/llm/jupyter-code-cell.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-chatty-response-thought.vue';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import { ref, computed } from 'vue';

const showThought = ref(false);

const showThoughtLabel = computed(() => (showThought.value ? 'Hide reasoning' : 'Show Reasoning'));
const showHideIcon = computed(() =>
	showThought.value ? 'pi pi-fw pi-eye-slash' : 'pi pi-fw pi-eye'
);

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

const showChatWindowMenu = (event) => chatWindowMenu.value.toggle(event);

const emit = defineEmits(['has-been-drawn']);

const props = defineProps<{
	jupyterSession: SessionContext;
	msg: { query: string; timestamp: string; messages: JupyterMessage[] };
	hasBeenDrawn: boolean;
	assetId?: string;
}>();

const thoughtHasBeenDrawn = () => {
	emit('has-been-drawn');
};
</script>
<style>
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

.jupyter-message {
	position: relative;
	padding: 5px;
	display: flex;
	flex-direction: column;
	background-color: var(--gray-100);
	font-family: var(--font-family);
	border-radius: 3px;
	margin-top: 10px;
}

.jupyter-message .absolute {
	display: none;
	position: absolute;
	top: 5px;
	right: 10px;
}

.jupyter-message:hover .absolute {
	display: block;
}

.response {
	color: green;
}

.date {
	font-family: var(--font-family);
}

.icon {
	padding: 5px;
}

.tool {
	padding-top: 3px;
	padding-left: 5px;
	vertical-align: top;
}

.thought-icon {
	padding: 5px;
}
</style>
