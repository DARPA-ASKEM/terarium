<template>
	<ul>
		<li v-for="{ target } in initials" :key="target">
			<div>
				<span>
					<h6>{{ target }}</h6>
					<span class="stretch-input">
						<tera-input label="Name" :model-value="getInitialName(props.model, target)"
					/></span>
				</span>
				<span class="stretch-input">
					<tera-input
						label="Description"
						:model-value="getInitialDescription(props.model, target)"
					/>
				</span>
			</div>
			<div>
				<tera-input label="Unit" :model-value="getInitialUnit(props.model, target)" />
				<tera-input label="Concept" :model-value="getInitialConcept(props.model, target)" />
			</div>
			<Divider type="solid" />
		</li>
	</ul>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { Model } from '@/types/Types';
import {
	getInitials,
	getInitialName,
	getInitialDescription,
	getInitialUnit,
	getInitialConcept
} from '@/model-representation/service';
import TeraInput from '@/components/widgets/tera-input.vue';
import Divider from 'primevue/divider';

const props = defineProps<{
	model: Model;
}>();

// const emit = defineEmits(['update-model']);

const initials = getInitials(props.model);

onMounted(() => {
	console.log(props.model);
});
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
