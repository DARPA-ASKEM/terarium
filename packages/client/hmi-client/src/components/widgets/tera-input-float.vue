<template>
	<InputText v-model="value" @keypress="handleKeypress" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InputText from 'primevue/inputtext';

const value = ref('');

const handleKeypress = (event) => {
	const charCode = event.which ? event.which : event.keyCode;
	if (charCode > 31 && (charCode < 48 || charCode > 57) && charCode !== 46) {
		event.preventDefault();
	} else if (charCode === 46 && event.target.value.indexOf('.') !== -1) {
		// Prevent entering more than one decimal point
		event.preventDefault();
	}
};
</script>
