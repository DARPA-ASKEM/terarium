<template>
	<p>
		Set the <strong>{{ config.intervention?.type }}</strong>
		<strong>{{ config.intervention?.appliedTo }}</strong> to
		<strong>{{ dynamicInterventions[0].threshold }}</strong> days when it
		<strong>{{
			dynamicInterventions[0].isGreaterThan ? 'increase to above' : 'decrease to below'
		}}</strong>
		the threshold value <strong>{{ dynamicInterventions[0].value }}</strong> person.
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
