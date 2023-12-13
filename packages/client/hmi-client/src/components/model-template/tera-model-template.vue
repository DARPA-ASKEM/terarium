<template>
	<tera-infinite-canvas>
		<template #foreground>
			<aside>
				<section v-if="model?.header?.schema_name">
					<h7>Model framework</h7>
					<h5>{{ model.header.schema_name }}<i class="pi pi-info-circle"></i></h5>
				</section>
				<section>
					<h7>Model templates</h7>
					<ul></ul>
				</section>
				<section class="trash">
					<i class="pi pi-trash"></i>
					<div>Drag items here to delete</div>
				</section>
			</aside>
		</template>
		<template #data>
			<tera-model-template-card />
		</template>
	</tera-infinite-canvas>
</template>

<script setup lang="ts">
import { Model } from '@/types/Types';
import TeraInfiniteCanvas from '../widgets/tera-infinite-canvas.vue';
import TeraModelTemplateCard from './tera-model-template-card.vue';

defineProps<{
	model?: Model;
}>();
</script>

<style scoped>
aside {
	width: 15rem;
	display: flex;
	flex-direction: column;
	height: 100%;
	background-color: #f4f7fa;
	border-right: 1px solid var(--surface-border-alt);
	padding: 1rem;
	gap: 0.5rem;
}

h5 {
	display: flex;
	align-items: center;
	gap: 0.25rem;
}

h5,
h7 {
	font-weight: var(--font-weight);
}

h7 {
	color: var(--text-color-subdued);
}

.pi-info-circle {
	color: var(--text-color-subdued);
	cursor: help;
}

.trash {
	margin-top: auto;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	border: 1px dashed #9fa9b7;
	border-radius: var(--border-radius);
	background-color: #eff2f5;
	text-align: center;
	padding: 1rem 0;

	& > .pi-trash {
		font-size: 1.5rem;
		margin-bottom: 0.5rem;
	}
}
</style>
