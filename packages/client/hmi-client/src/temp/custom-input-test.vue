<template>
	<div class="flex flex-column pl-8 gap-5">
		<div class="w-2">
			<label>normal input</label>
			<tera-input-field v-model="teststring" label="Label" />
		</div>

		<div class="w-2">
			<label>Error input</label>
			<tera-input-field
				label="Label"
				:error-message="errormessage"
				v-model="errorstring"
				@update:model-value="onUpdate"
			/>
		</div>

		<div class="w-2">
			<label>Disabled input</label>
			<tera-input-field label="Label" model-value="Disabled" disabled />
		</div>

		<div class="w-2">
			<label>Number input</label>
			<tera-input-field label="Label" v-model="numberstring" type="number" />
		</div>

		<p>whacky tests, counter = {{ whackyCounter }}</p>
		<div v-for="(n, idx) in whackyComputed.listing" :key="n" class="w-2">
			<tera-input-field :model-value="n" @update:model-value="(v) => update2(v, idx)" />
		</div>
	</div>
</template>

<script setup lang="ts">
import teraInputField from '@/components/widgets/tera-input-field.vue';
import { ref, computed } from 'vue';

const teststring = ref<string>('');
const numberstring = ref<string>('');
const errorstring = ref<string>('Error message error message');
const errormessage = ref<string>('Error message error message');

const whackyVars = ref(['first', 'second']);
const whackyCounter = ref(0);
const update2 = (v: string, idx: number) => {
	whackyVars.value[idx] = v;
};

const whackyComputed = computed(() => {
	whackyCounter.value++;
	return { listing: [whackyVars.value[0], whackyVars.value[1]] };
});

function onUpdate(value: string) {
	if (value) {
		errormessage.value = 'Error message error message';
	} else {
		errormessage.value = '';
	}
}
</script>
