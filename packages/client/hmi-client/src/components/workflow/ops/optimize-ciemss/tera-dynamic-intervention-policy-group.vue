<template>
	<div class="policy-group">
		<div class="form-header">
			<h6 class="mr-auto">{{ config.interventionName ?? `Intervention` }}</h6>
		</div>
		<p>
			Set the {{ config.individualIntervention.type }}&nbsp;
			<strong>{{ config.individualIntervention.appliedTo }}</strong> to
			<strong>{{ config.individualIntervention.value }}</strong> when it crosses the threshold value
			<strong>{{ config.individualIntervention.threshold }}</strong>
		</p>
	</div>
</template>
<script setup lang="ts">
import { DynamicIntervention } from '@/types/Types';
import { InterventionPolicyGroupForm } from './optimize-ciemss-operation';

defineProps<{
	config: DynamicInterventionPolicyGroupForm;
}>();

export interface DynamicInterventionPolicyGroupForm extends InterventionPolicyGroupForm {
	individualIntervention: DynamicIntervention;
}

const emit = defineEmits(['update-self']);
</script>
<style>
.form-header {
	width: 100%;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	align-items: center;
	gap: var(--gap-4);
	padding-bottom: 0.5rem;

	& > *:first-child {
		margin-right: auto;
	}

	& > * {
		display: flex;
		flex-direction: row;
		justify-content: space-between;
		align-items: center;
		gap: var(--gap-2);
	}
}

.policy-group {
	display: flex;
	padding: var(--gap-4);
	padding-left: var(--gap-5);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: var(--gap-2);
	border-radius: var(--gap-1-5);
	background: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}
.policy-group + .policy-group {
	margin-top: var(--gap-2);
}
</style>
