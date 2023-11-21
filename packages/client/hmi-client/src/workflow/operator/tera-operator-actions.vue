<template>
	<Button
		v-for="({ label, action, isPrimary, icon }, index) in actionButtons"
		:key="index"
		:label="label"
		:icon="icon"
		:severity="!isPrimary ? 'secondary' : undefined"
		:outlined="!isPrimary"
		@click="action()"
	/>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { OperatorActionButton } from '@/types/workflow';
import Button from 'primevue/button';

defineProps({
	actionButtons: {
		type: Array as PropType<OperatorActionButton[]>,
		default: () => []
	}
});
</script>
