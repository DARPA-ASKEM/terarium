<template>
	<div class="response-thought-container">
		<div
			ref="responseThought"
			class="thought"
			:class="{ hide: hasBeenDrawn && !props.showThought, show: props.showThought }"
		>
			{{ displayedText }}<span v-if="!hasBeenDrawn" class="blinking-cursor">|</span>
		</div>
	</div>
</template>
<script setup lang="ts">
import { ref, watchEffect, computed } from 'vue';
// import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';

const emit = defineEmits(['has-been-drawn', 'delete']);
const responseThought = ref(<HTMLElement | null>null);
const hasBeenDrawn = ref(false);

const props = defineProps<{
	thought: string;
	showThought?: boolean;
	hasBeenDrawn?: boolean;
}>();

const charIndex = ref(0);

watchEffect(() => {
	const typing = setInterval(() => {
		// responseThought.value?.scrollTo();
		charIndex.value++;
		if (charIndex.value > props.thought.length) {
			clearInterval(typing);
			hasBeenDrawn.value = true;
			emit('has-been-drawn');
		}
	}, 10); // adjust speed here
});

const displayedText = computed(() =>
	charIndex.value < props.thought.length
		? `${props.thought.slice(0, charIndex.value)}`
		: props.thought
);
</script>

<style scoped>
.thought {
	border-radius: 5px;
	white-space: pre-line;
	color: var(--gray-600);
	max-height: 10000px;
	overflow: hidden;
	padding: 5px;
}
.hide {
	max-height: 0;
	overflow: hidden;
	margin: 0;
	opacity: 0;
	padding: 0;
	transition: opacity 2s, margin 3s ease-in, padding 3s ease-in, max-height 2.2s ease-in;
}

.show {
	transition: margin 2.5s ease-in, opacity 1s ease-in, max-height 2s ease-in;
}

.blinking-cursor {
	margin-left: 5px;
	width: 8px;
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

.response-thought-container {
	display: flex;
	flex-direction: column;
}
</style>
