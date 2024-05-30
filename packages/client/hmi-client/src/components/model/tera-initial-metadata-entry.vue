<template>
	<section>
		<div class="name">
			<h6>{{ target }}</h6>
			<span>
				<tera-input
					label="Name"
					:model-value="getInitialName(model, target)"
					@update:model-value="$emit('update-initial-metadata', { key: 'name', value: $event })"
				/>
			</span>
		</div>
		<tera-input
			label="Description"
			:model-value="getInitialDescription(model, target)"
			@update:model-value="
				$emit('update-initial-metadata', {
					key: 'description',
					value: $event
				})
			"
		/>
		<tera-input
			label="Unit"
			:model-value="getInitialUnits(model, target)"
			@update:model-value="$emit('update-initial-metadata', { key: 'units', value: $event })"
		/>
		<tera-input
			label="Concept"
			:model-value="getInitialConcept(model, target)"
			disabled
			@update:model-value="
				$emit('update-initial-metadata', {
					key: 'concept',
					value: $event
				})
			"
		/>
	</section>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import {
	getInitialName,
	getInitialDescription,
	getInitialUnits,
	getInitialConcept
} from '@/model-representation/service';
import TeraInput from '@/components/widgets/tera-input.vue';

defineProps<{
	model: Model;
	target: string;
}>();
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
