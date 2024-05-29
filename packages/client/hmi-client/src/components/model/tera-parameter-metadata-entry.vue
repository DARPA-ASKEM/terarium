<template>
	<section>
		<div>
			<span>
				<h6>{{ id }}</h6>
				<span class="stretch-input">
					<tera-input
						label="Name"
						:model-value="name ?? ''"
						@update:model-value="$emit('update-parameter', { key: 'name', value: $event })"
					/>
				</span>
			</span>
			<span class="stretch-input">
				<tera-input
					label="Description"
					:model-value="description ?? ''"
					@update:model-value="$emit('update-parameter', { key: 'description', value: $event })"
				/>
			</span>
		</div>
		<div>
			<tera-input
				label="Unit"
				:model-value="units?.expression ?? ''"
				@update:model-value="$emit('update-parameter', { key: 'units', value: $event })"
			/>
			<tera-input
				label="Concept"
				:model-value="grounding?.identifiers[0] ?? ''"
				@update:model-value="$emit('update-parameter', { key: 'grounding', value: $event })"
			/>
		</div>
	</section>
</template>

<script setup lang="ts">
import type { ModelParameter } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';

const props = defineProps<{
	parameter: ModelParameter;
}>();

defineEmits(['update-parameter']);

const { id, name, description, grounding, units } = props.parameter;
</script>

<style scoped>
div {
	display: flex;
	gap: var(--gap-small);

	& > :first-child {
		display: flex;
		align-items: center;
		gap: var(--gap-small);
		width: 20%;
	}
}

.stretch-input {
	flex-grow: 1;
}
</style>
