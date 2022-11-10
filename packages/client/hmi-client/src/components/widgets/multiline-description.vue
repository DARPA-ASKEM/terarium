<template>
	<p v-for="(p, idx) in paragraphs" :key="idx">
		{{ p }}
	</p>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from 'vue';

// Split newline sequences
export default defineComponent({
	name: 'MultilineDescription',
	props: {
		text: {
			type: String as PropType<string>,
			required: true
		}
	},
	setup(props) {
		const paragraphs = computed(() => props.text?.split(/\r|\n/).filter((d) => d !== '') ?? []);
		return {
			paragraphs
		};
	}
});
</script>
