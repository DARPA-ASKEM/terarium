<template>
	<header class="flex">
		<tera-edit-value
			class="mr-auto"
			:model-value="intervention.name"
			@update:model-value="onUpdateName($event)"
		/>
		<Button icon="pi pi-trash" text @click="console.log('delete')" />
	</header>
	<section></section>
	<footer></footer>
</template>

<script setup lang="ts">
import TeraEditValue from '@/components/widgets/tera-edit-value.vue';
import Button from 'primevue/button';
import { DummyIntervention } from './tera-interventions-operation';

const emit = defineEmits(['update', 'delete']);
const props = defineProps<{
	intervention: DummyIntervention;
}>();

const onUpdateName = (name: string) => {
	emit('update', { ...props.intervention, name });
};
</script>
