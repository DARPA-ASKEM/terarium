<template>
	<p>
		Set the <b>{{ config.intervention?.type }}</b> <b>{{ config.intervention?.appliedTo }}</b> to
		<b>{{ dynamicInterventions[0].threshold }}</b> days when it
		<b>{{ dynamicInterventions[0].isGreaterThan ? 'increase to above' : 'decrease to below' }}</b>
		the threshold value <b>{{ dynamicInterventions[0].value }}</b> person.
	</p>
</template>
<script setup lang="ts">
import { DynamicIntervention } from '@/types/Types';
import { InterventionPolicyGroup } from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';

defineProps<{
	config: InterventionPolicyGroup;
	dynamicInterventions: DynamicIntervention[];
}>();
</script>

<style scoped></style>
