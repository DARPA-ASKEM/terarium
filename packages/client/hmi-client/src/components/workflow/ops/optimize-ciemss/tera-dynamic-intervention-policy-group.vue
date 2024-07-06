<template>
	<p>
		Set the parameter <b>{{ config.intervention.appliedTo }}</b> to
		<b>{{ dynamicInterventions[0].threshold }}</b> days when it
		<b>{{ dynamicInterventions[0].isGreaterThan ? 'increase to above' : 'decrease to below' }}</b>
		the threshold value <b>{{ dynamicInterventions[0].value }}</b> person.
	</p>
</template>
<script setup lang="ts">
defineProps<{
	config: any;
	dynamicInterventions: any;
}>();
</script>

<style scoped></style>
