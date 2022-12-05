<template>
	<p v-for="(p, idx) in paragraphs" :key="idx">
		{{ p }}
	</p>
</template>

<script setup lang="ts">
import { PropType, computed } from 'vue';

// Split newline sequences
const props = defineProps({
	text: {
		type: String as PropType<string>,
		required: true
	}
});

const paragraphs = computed(() => props.text?.split(/\r|\n/).filter((d) => d !== '') ?? []);
</script>
