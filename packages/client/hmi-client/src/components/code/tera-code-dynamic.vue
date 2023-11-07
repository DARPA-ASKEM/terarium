<template>
	<Card class="code-dynamic-card-container">
		<template #title>
			Code Block
			<div>
				<Button text icon="pi pi-pencil" @click="editable = !editable" />
				<Button text icon="pi pi-trash" />
			</div>
		</template>
		<template #content>
			<div v-if="!editable">Lines 1 to 9</div>
			<div class="edit-container" v-else>
				<div class="edit-input-container">
					<label for="code-dynamic-start">Start</label>
					<InputNumber input-id="code-dynamic-start" />
				</div>

				<div class="edit-input-container">
					<label for="code-dynamic-start">End</label>
					<InputNumber input-id="code-dynamic-end" />
				</div>
			</div>
		</template>
	</Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import InputNumber from 'primevue/inputnumber';
import Button from 'primevue/button';
import { ref } from 'vue';

defineProps<{
	dynamic: string;
}>();

const editable = ref(false);
</script>

<style scoped>
.code-dynamic-card-container {
	width: 240px;
	max-width: 240px;
}

.code-dynamic-card-container:deep(.p-card-title) {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.edit-container {
	display: flex;
}

.edit-input-container:deep(.p-inputnumber-input) {
	width: 100px;
}
</style>
