<template>
	<section>
		<div>
			<span>
				<h6>{{ target }}</h6>
				<span class="stretch-input">
					<tera-input
						label="Name"
						:model-value="getInitialName(model, target)"
						@update:model-value="$emit('update-initial-metadata', { key: 'name', value: $event })"
				/></span>
			</span>
			<span class="stretch-input">
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
			</span>
		</div>
		<div>
			<tera-input
				label="Unit"
				:model-value="getInitialUnits(model, target)"
				@update:model-value="$emit('update-initial-metadata', { key: 'units', value: $event })"
			/>
			<tera-input
				label="Concept"
				:model-value="getInitialConcept(model, target)"
				@update:model-value="
					$emit('update-initial-metadata', {
						key: ['concept', 'grounding'],
						value: $event
					})
				"
			/>
		</div>
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
