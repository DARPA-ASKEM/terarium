<template>
	<section>
		<vue-feather type="columns" size="1.25rem" />
		<slot />
	</section>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { OperatorStatus } from '@/types/workflow';

defineProps({
	status: {
		type: String as PropType<OperatorStatus>,
		default: OperatorStatus.DEFAULT
	}
});
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
}
</style>
