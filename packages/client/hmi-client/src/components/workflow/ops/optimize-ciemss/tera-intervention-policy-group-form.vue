<template>
	<div class="policy-group">
		<div class="form-header">
			<div>
				<InputText
					v-if="isEditing"
					v-model="config.name"
					placeholder="Policy bounds"
					@focusout="emit('update-self', config)"
				/>
				<h6 v-else>{{ config.name }}</h6>
				<i
					:class="{ 'pi pi-check i': isEditing, 'pi pi-pencil i': !isEditing }"
					:style="'cursor: pointer'"
					@click="onEdit"
				/>
			</div>
			<div>
				<label for="active">Optimize</label>
				<InputSwitch
					v-model="config.isActive"
					:disabled="config.isDisabled"
					@change="emit('update-self', config)"
				/>
			</div>
		</div>

		<template v-if="dynamicInterventions.length > 0">
			<tera-dynamic-intervention-policy-group
				:config="config"
				@update-self="updateState"
				:dynamicInterventions="dynamicInterventions"
			/>
		</template>
		<template v-else-if="staticInterventions.length > 0">
			<tera-static-intervention-policy-group
				:config="config"
				@update-self="updateState"
				:staticInterventions="staticInterventions"
			/>
		</template>
	</div>
</template>

<script setup lang="ts">
import { cloneDeep } from 'lodash';
import { ref } from 'vue';
import InputText from 'primevue/inputtext';
import InputSwitch from 'primevue/inputswitch';
import {
	InterventionPolicyGroup,
	InterventionTypes
} from '@/components/workflow/ops/optimize-ciemss/optimize-ciemss-operation';
import TeraStaticInterventionPolicyGroup from '@/components/workflow/ops/optimize-ciemss/tera-static-intervention-policy-group.vue';
import TeraDynamicInterventionPolicyGroup from '@/components/workflow/ops/optimize-ciemss/tera-dynamic-intervention-policy-group.vue';
import { DynamicIntervention, StaticIntervention } from '@/types/Types';

const props = defineProps<{
	parameterOptions: string[];
	config: InterventionPolicyGroup;
	interventionType?: InterventionTypes;
}>();

const emit = defineEmits(['update-self', 'delete-self']);

const dynamicInterventions = ref<DynamicIntervention[] | []>(
	props.config.intervention?.dynamicInterventions ?? []
);
const staticInterventions = ref<StaticIntervention[] | []>(
	props.config.intervention?.staticInterventions ?? []
);

const config = ref<InterventionPolicyGroup>(cloneDeep(props.config));
const isEditing = ref<boolean>(false);

const onEdit = () => {
	isEditing.value = !isEditing.value;
};

const updateState = () => {
	emit('update-self', config);
};
</script>

<style scoped>
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
</style>
