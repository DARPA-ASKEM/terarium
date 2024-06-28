<template>
	<div>
		<header class="flex align-items-center">
			<tera-edit-value
				class="mr-auto"
				:model-value="intervention.name"
				@update:model-value="onUpdateName($event)"
				tag="h5"
			/>
			<RadioButton
				v-model="interventionType"
				input-id="static"
				name="interventionType"
				value="static"
			/>
			<label for="static" class="ml-2">Static</label>
			<RadioButton
				v-model="interventionType"
				input-id="dyanamic"
				name="interventionType"
				value="dynamic"
				class="ml-3"
			/>
			<label for="dynamic" class="ml-2">Dynamic</label>
			<Button class="ml-3" icon="pi pi-trash" text @click="console.log('delete')" />
		</header>
		<section></section>
		<footer>
			<Button text icon="pi pi-plus" label="Add" @click="console.log('add')" size="small" />
		</footer>
	</div>
</template>

<script setup lang="ts">
import TeraEditValue from '@/components/widgets/tera-edit-value.vue';
import Button from 'primevue/button';
import RadioButton from 'primevue/radiobutton';
import { ref } from 'vue';
import { DummyIntervention } from './tera-interventions-operation';

const emit = defineEmits(['update', 'delete']);
const props = defineProps<{
	intervention: DummyIntervention;
}>();

const interventionType = ref('static');

const onUpdateName = (name: string) => {
	emit('update', { ...props.intervention, name });
};
</script>
