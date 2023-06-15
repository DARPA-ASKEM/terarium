<template>
	<template>
		<h5>Thoguht</h5>
		<div class="thought" ref="elem">
			{{ startTyping(props.thought, 0, 100) }}
		</div>
	</template>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
	thought: string;
}>();

const elem = ref(<HTMLElement | null>null);

function startTyping(text: string, charIndex: number, speed: number) {
	if (elem.value) {
		if (charIndex < text.length) {
			elem.value.innerHTML = `${text.slice(0, charIndex)}<span class="blinking-cursor">|</span>`;
			charIndex++;
			setTimeout(() => {
				startTyping(text, charIndex, speed);
			}, speed);
		} else {
			elem.value.innerHTML = `${text.slice(0, charIndex)}`;
		}
	}
}
</script>

<style scoped>
.thought {
	padding: 5px;
	white-space: pre-line;
	color: green;
}

.thought:hover {
	white-space: pre-line;
	border: 2px solid var(--gray-500);
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
