<template>
	<ul>
		<li v-for="{ id, name, description, grounding, units } in parameters" :key="id">
			<div>
				<span>
					<h6>{{ id }}</h6>
					<span class="stretch-input">
						<tera-input label="Name" :model-value="name ?? ''" />
					</span>
				</span>
				<span class="stretch-input">
					<tera-input label="Description" :model-value="description ?? ''" />
				</span>
			</div>
			<div>
				<tera-input label="Unit" :model-value="units?.expression ?? ''" />
				<tera-input label="Concept" :model-value="grounding?.identifiers[0]" />
			</div>
			<Divider type="solid" />
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import { getParameters } from '@/model-representation/service';
import TeraInput from '@/components/widgets/tera-input.vue';
import Divider from 'primevue/divider';

const props = defineProps<{
	model: Model;
}>();

// const emit = defineEmits(['update-model']);

const parameters = getParameters(props.model);
</script>

<style scoped>
ul > li {
	list-style: none;

	& > div {
		display: flex;
		gap: var(--gap-small);

		& > :first-child {
			display: flex;
			align-items: center;
			gap: var(--gap-small);
			width: 20%;
		}
	}
}

.stretch-input {
	flex-grow: 1;
}

:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: 0;
		margin-bottom: var(--gap);
		color: var(--gray-300);
	}
	&.p-divider-vertical {
		margin-left: var(--gap-small);
		margin-right: var(--gap);
	}
}
</style>
