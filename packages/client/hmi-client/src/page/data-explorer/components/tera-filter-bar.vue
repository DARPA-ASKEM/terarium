<template>
	<div class="flex align-items-center">
		<label class="mr-2">Topic Filter</label>
		<Dropdown class="topic-dropdown" optionLabel="label" placeholder="Select a Topic" />
	</div>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
</script>

<style scoped>
.topic-dropdown {
	min-width: 12rem;
}
</style>
