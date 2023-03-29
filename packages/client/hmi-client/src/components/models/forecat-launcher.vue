<template>
	<Modal class="modal" @modal-mask-clicked="close()">
		<template #default>
			<!-- initial values -->
			<h4>Initial values</h4>
			<section>
				<div v-for="(s, i) of props.model.content.S" :key="i" class="row">
					<span>{{ s.sname }}</span>
					<InputText type="text" class="p-inputtext-sm" v-model.number="initialValues[s.sname]" />
				</div>
			</section>

			<!-- params -->
			<h4>Parameter values</h4>
			<section>
				<div v-for="(t, i) of props.model.content.T" :key="i" class="row">
					<span>{{ t.tname }}</span>
					<InputText type="text" class="p-inputtext-sm" v-model.number="parameterValues[t.tname]" />
				</div>
			</section>
		</template>
		<template #footer>
			<Button @click="launch">Launch</Button>
			<Button class="p-button-secondary" @click="close()">Cancel</Button>
		</template>
	</Modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ITypedModel } from '@/types/Model';
import { PetriNet } from '@/petrinet/petrinet-service';
import Modal from '@/components/widgets/Modal.vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';

interface NumericValueMap {
	[key: string]: number;
}

const props = defineProps<{
	model: ITypedModel<PetriNet>;
}>();

const initialValues = ref<NumericValueMap>({});
const parameterValues = ref<NumericValueMap>({});

props.model.content.S.forEach((s) => {
	initialValues.value[s.sname] = 1;
});
props.model.content.T.forEach((s) => {
	parameterValues.value[s.tname] = 0.5;
});

const emit = defineEmits(['close', 'launch-forecast']);

const launch = () => {
	console.log(initialValues.value);
	console.log(parameterValues.value);
};

const close = () => {
	emit('close');
};
</script>

<style scoped>
.row {
	display: flex;
	flex-direction: row;
	justify-content: flex-end;
	padding-top: 2px;
	padding-bottom: 2px;
}
.p-inputtext.p-inputtext-sm {
	padding: 0.25rem 0.5rem;
	font-size: 1rem;
	margin-left: 1rem;
}
</style>
