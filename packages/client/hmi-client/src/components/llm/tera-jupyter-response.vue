<template>
	<div>
		<div class="container">
			<div v-for="msg in props.message" :key="msg.header.msg_id">
				<div
					v-if="msg.header.msg_type === 'llm_response' && msg.content['name'] === 'response_text'"
				>
					<div class="response">{{ msg.content['chatty_response'] }}</div>
				</div>
				<div v-else-if="msg.header.msg_type === 'stream' && msg.content['name'] === 'stderr'">
					<div class="error">{{ msg.content['text'] }}</div>
				</div>
				<div v-else-if="msg.header.msg_type === 'llm_request'">
					<h2 class="query">
						{{ msg.content['request'] }}
					</h2>
					<div class="date">{{ msg.header.date }}</div>
				</div>
				<div v-else-if="msg.header.msg_type === 'code_cell'" class="code-cell">
					<jupyter-code-cell
						:jupyter-session="jupyterSession"
						:language="msg.content['language']"
						:code="msg.content['code']"
						:autorun="true"
						context="dataset"
						:context_info="{
							id:
								props.assetId !== undefined ? props.assetId : 'a035cc6f-e1a5-416b-9320-c3822255ab19'
						}"
					/>
				</div>
			</div>
		</div>
	</div>
</template>
<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
// 'stream', 'code_cell', 'llm_request', 'chatty_response'
const props = defineProps<{
	message: JupyterMessage[];
	assetId?: string;
}>();
</script>
<style scoped>
.error {
	color: darkred;
	white-space: pre-wrap;
	padding-top: 10px;
}

.container {
	display: flex;
	flex-direction: column;
	background-color: var(--gray-50);
	font-family: var(--font-family);
	border-radius: 3px;
	padding-top: 20px;
	margin-top: 10px;
}

.response {
	color: green;
}

.query {
	padding-bottom: 5px;
}

.date {
	font-family: var(--font-family);
}
</style>
