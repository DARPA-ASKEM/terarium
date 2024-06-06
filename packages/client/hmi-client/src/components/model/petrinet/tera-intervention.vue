<template>
	<div class="flex flex-column gap-2">
		<header class="flex align-items-center">
			<div>
				<strong>{{ parameterId }}</strong>
				<span v-if="name" class="ml-1">{{ '| ' + name }}</span>
				<span v-if="unit" class="ml-2">({{ unit }})</span>
				<span v-if="description" class="ml-4">{{ description }}</span>
			</div>
			<Button class="ml-auto" icon="pi pi-trash" text />
		</header>
		<ul class="flex flex-column gap-2">
			<li class="flex gap-2" v-for="(intervention, idx) in interventions" :key="idx">
				<tera-input label="Time step" type="nist" :model-value="intervention.timestep" />
				<tera-input label="Value" type="nist" :model-value="intervention.value" />
				<Button text small icon="pi pi-times" @click="emit('delete-intervention', intervention)" />
			</li>
		</ul>
		<span>
			<Button text size="small" icon="pi pi-plus" label="Add time step" />
		</span>
	</div>
</template>

<script setup lang="ts">
import { getParameter } from '@/model-representation/service';
import { Intervention, Model } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';
import Button from 'primevue/button';

const props = defineProps<{
	model: Model;
	parameterId: string;
	interventions: Intervention[];
}>();

const emit = defineEmits(['delete-intervention']);

const name = getParameter(props.model, props.parameterId)?.name;
const unit = getParameter(props.model, props.parameterId)?.units?.expression;
const description = getParameter(props.model, props.parameterId)?.description;
</script>
