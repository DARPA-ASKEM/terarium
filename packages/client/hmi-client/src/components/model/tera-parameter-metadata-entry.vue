<template>
	<section>
		<div class="name">
			<h6>{{ id }}</h6>
			<span>
				<tera-input
					label="Name"
					:model-value="name ?? ''"
					@update:model-value="$emit('update-parameter', { key: 'name', value: $event })"
				/>
			</span>
		</div>
		<tera-input
			label="Description"
			:model-value="description ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'description', value: $event })"
		/>
		<tera-input
			label="Unit"
			:model-value="units?.expression ?? ''"
			@update:model-value="$emit('update-parameter', { key: 'units', value: $event })"
		/>
		<tera-input
			label="Concept"
			disabled
			:model-value="
				grounding?.identifiers
					? getNameOfCurieCached(
							new Map<string, string>(),
							getCurieFromGroundingIdentifier(grounding.identifiers)
						)
					: ''
			"
			@update:model-value="$emit('update-parameter', { key: 'grounding', value: $event })"
		/>
	</section>
</template>

<script setup lang="ts">
import type { ModelParameter } from '@/types/Types';
import TeraInput from '@/components/widgets/tera-input.vue';
import { getCurieFromGroundingIdentifier, getNameOfCurieCached } from '@/services/concept';

const props = defineProps<{
	parameter: ModelParameter;
}>();

defineEmits(['update-parameter']);

const { id, name, description, grounding, units } = props.parameter;
</script>

<style scoped>
section {
	display: grid;
	grid-template-columns: 30% 1fr;
	gap: var(--gap-small);

	& > .name {
		display: flex;
		align-items: center;
		gap: var(--gap-small);

		& > span {
			flex-grow: 1;
		}
	}

	& > :last-child {
		width: 45%;
	}
}
</style>
