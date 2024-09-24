<template>
	<div class="policy-group">
		<div class="form-header">
			<label class="mr-auto" tag="h5"> {{ config.intervention?.name ?? `Intervention` }}</label>
			<aside>
				<label for="active">Optimize</label>
				<InputSwitch v-model="knobs.isActive" :disabled="true" @change="emit('update-self', knobs)" />
			</aside>
		</div>
		<p>
			Set the {{ config.intervention?.type }}&nbsp; <strong>{{ config.intervention?.appliedTo }}</strong> to
			<strong>{{ dynamicInterventions[0].threshold }}</strong> days when it crosses the threshold value
			<strong>{{ dynamicInterventions[0].value }}</strong> person.
		</p>
	</div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue';
import { DynamicIntervention } from '@/types/Types';
import { InterventionPolicyGroupForm } from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';
import InputSwitch from 'primevue/inputswitch';

const props = defineProps<{
	config: InterventionPolicyGroupForm;
}>();

const emit = defineEmits(['update-self']);

const dynamicInterventions = computed<DynamicIntervention[]>(() => props.config.intervention.dynamicInterventions);

const knobs = ref({
	isActive: props.config.isActive ?? false
});
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
