<template>
	<tera-variables
		:variable-list="transitionsList"
		:disabled-inputs="['concept', 'description', 'unitExpression']"
		@update-variable="$emit('update-transition', $event)"
	/>
</template>

<script setup lang="ts">
import TeraVariables from '@/components/model/variables/tera-variables.vue';

defineEmits(['update-transition']);

const transitionsList = [];
</script>
