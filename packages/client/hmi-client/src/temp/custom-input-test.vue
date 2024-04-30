<template>
	<div>
		<div>
			<label>normal input</label>
			<tera-input-field />
		</div>

		<div>
			<label>Error input</label>
			<tera-input-field error-message="Error message error message" />
		</div>
	</div>
</template>

<script setup lang="ts">
import teraInputField from '@/components/widgets/tera-input-field.vue';
</script>
