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

		<div class="w-2">
			<label>sci input</label>
			<tera-input-field
				v-model="scistring"
				label="Label"
				type="nist"
				@update:model-value="console.log(scistring)"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import teraInputField from '@/components/widgets/tera-input-field.vue';
import { ref } from 'vue';

const teststring = ref<string>('');
const numberstring = ref<string>('');
const scistring = ref<string>('1234.12345');
const errorstring = ref<string>('Error message error message');
const errormessage = ref<string>('Error message error message');

function onUpdate(value: string) {
	if (value) {
		errormessage.value = 'Error message error message';
	} else {
		errormessage.value = '';
	}
}
</script>
