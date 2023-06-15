<template>
	<div>
		<div class="container">
			<div v-if="msg.header.msg_type === 'llm_request'">
				<h2 class="query">
					{{ msg.content['request'] }}
				</h2>
				<div class="date">{{ msg.header.date }}</div>
			</div>
			<div
				v-else-if="
					msg.header.msg_type === 'llm_response' && msg.content['name'] === 'response_text'
				"
			>
				<div class="response">{{ msg.content['chatty_response'] }}</div>
			</div>
			<div v-else-if="msg.header.msg_type === 'stream' && msg.content['name'] === 'stderr'">
				<div class="error">{{ msg.content['text'] }}</div>
			</div>
			<div v-else-if="msg.header.msg_type === 'stream' && msg.content['name'] === 'stdout'">
				<tera-jupyter-response-thought :thought="msg.content['text']" />
			</div>
			<div v-else-if="msg.header.msg_type === 'code_cell'" class="code-cell">
				<jupyter-code-cell
					:jupyter-session="jupyterSession"
					:language="msg.content['language']"
					:code="msg.content['code']"
					:autorun="true"
					context="dataset"
					:context_info="{
						id: props.assetId
					}"
				/>
			</div>
		</div>
	</div>
</template>
<script setup lang="ts">
import { JupyterMessage } from '@/services/jupyter';
import { SessionContext } from '@jupyterlab/apputils';
import JupyterCodeCell from '@/components/llm/jupyter-code-cell.vue';
import TeraJupyterResponseThought from '@/components/llm/tera-chatty-response-thought.vue';

// <div class="thought" :ref="(el) => refHandler(el, 0)">
// 					{{ startTyping(0, cleanMessage(msg.content['text']), 0, 100) }}
// 				</div>

// interface MessageObj {
// 	element: HTMLElement | null;
// 	hasBeenDrawn: Boolean;
// }

// const messageObj = ref([] as MessageObj[]);

// const refHandler = (el, i: number) => {
// 	if (el) {
// 		const newMessageObj: MessageObj = {
// 			element: el,
// 			hasBeenDrawn: !!(messageObj.value[i] && messageObj.value[i].hasBeenDrawn)
// 		};
// 		messageObj.value[i] = newMessageObj;
// 	}
// };

// const cleanMessage = (message:string) =>message.replaceAll("\n", "\n\n").replaceAll(":", " :\n\n").trim();

const props = defineProps<{
	jupyterSession: SessionContext;
	msg: JupyterMessage;
	assetId?: string;
}>();

// function startTyping(idx: number, text: string, index: number, speed: number) {
// 	if (messageObj.value[idx]) {
// 		const elem = messageObj.value[idx].element;
// 		if (elem) {
// 			if (index < text.length) {
// 				elem.innerHTML = `${text.slice(0, index)}<span class="blinking-cursor">|</span>`;
// 				index++;
// 				setTimeout(() => {
// 					startTyping(idx, text, index, speed);
// 				}, speed);
// 			} else {
// 				messageObj.value[idx].hasBeenDrawn = true;
// 				elem.innerHTML = `${text.slice(0, index)}`;
// 			}
// 		}
// 	}
// }
</script>
<style>
.error {
	color: darkred;
	white-space: pre-wrap;
	padding-top: 10px;
}

.container {
	display: flex;
	flex-direction: column;
	background-color: var(--gray-100);
	font-family: var(--font-family);
	border-radius: 3px;
	margin-top: 10px;
}

.thought {
	padding: 5px;
	white-space: pre-line;
	color: green;
}

.thought:hover {
	white-space: pre-line;
	border: 2px solid var(--gray-500);
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

.blinking-cursor {
	margin-left: 5px;
	width: 8px;
	background-color: green;
	animation: blink 1s infinite;
}

@keyframes blink {
	0%,
	50% {
		opacity: 1;
	}
	50.1%,
	100% {
		opacity: 0;
	}
}
</style>
