<template>
	<div class="flex flex-column pl-8 gap-5">
		<div class="w-2">
			<label>normal input</label>
			<tera-input-text v-model="teststring" label="Label" />
		</div>

		<div class="w-2">
			<label>Error input</label>
			<tera-input-text
				label="Label"
				:error-message="errormessage"
				v-model="errorstring"
				@update:model-value="onUpdate"
			/>
		</div>

		<div class="w-2">
			<label>Disabled input</label>
			<tera-input-text label="Label" model-value="Disabled" disabled />
		</div>

		<div class="w-2">
			<label>Number input</label>
			<tera-input-number label="Label" v-model="numberstring" />
		</div>

		<div class="w-2">
			<label>sci input</label>
			<tera-input-number v-model="scistring" label="Label" @update:model-value="console.log(scistring)" />
		</div>
	</div>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { ref } from 'vue';

const teststring = ref<string>('');
const numberstring = ref<number>();
const scistring = ref<number>(1234.12345);
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
