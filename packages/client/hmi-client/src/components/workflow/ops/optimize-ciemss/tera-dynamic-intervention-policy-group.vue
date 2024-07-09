<template>
	<div class="policy-group">
		<div class="form-header">
			<tera-toggleable-edit
				class="mr-auto"
				:model-value="knobs.name"
				@update:model-value="onEditName($event)"
				tag="h5"
			/>
			<div>
				<!-- TODO: We should be able to utilize dynamic in the future -->
				<label for="active">Optimize</label>
				<InputSwitch
					v-model="knobs.isActive"
					:disabled="true"
					@change="emit('update-self', knobs)"
				/>
			</div>
		</div>
		<p>
			Set the <strong>{{ config.intervention?.type }}</strong>
			<strong>{{ config.intervention?.appliedTo }}</strong> to
			<strong>{{ dynamicInterventions[0].threshold }}</strong> days when it
			<strong>{{
				dynamicInterventions[0].isGreaterThan ? 'increase to above' : 'decrease to below'
			}}</strong>
			the threshold value <strong>{{ dynamicInterventions[0].value }}</strong> person.
		</p>
	</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { DynamicIntervention } from '@/types/Types';
import { InterventionPolicyGroup } from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';
import InputSwitch from 'primevue/inputswitch';
import TeraToggleableEdit from '@/components/widgets/tera-toggleable-edit.vue';

const props = defineProps<{
	config: InterventionPolicyGroup;
	dynamicInterventions: DynamicIntervention[];
}>();

const emit = defineEmits(['update-self']);

const knobs = ref({
	name: props.config.name ?? 'policy bounds',
	isActive: props.config.isActive ?? false
});

const onEditName = (name: string) => {
	knobs.value.name = name;
	emit('update-self', knobs);
};
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
	padding: 1rem 1rem 1rem 1.5rem;
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: var(--gap-2);
	border-radius: 0.375rem;
	background: #fff;
	border: 1px solid rgba(0, 0, 0, 0.08);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}
</style>
