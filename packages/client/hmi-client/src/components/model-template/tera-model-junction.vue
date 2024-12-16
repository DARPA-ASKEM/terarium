<template>
	<section>
		<i class="pi pi-info-circle" />
		<ul>
			<li v-for="(edge, index) in junction.edges" :key="index">
				{{ junction.edges[0].target.portId }}
				=
				{{ cards.find(({ id }) => id === edge.target.cardId)?.model.header.name }}.{{ edge.target.portId }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import type { ModelTemplateCard, ModelTemplateJunction } from '@/types/model-templating';

defineProps<{
	junction: ModelTemplateJunction;
	cards: ModelTemplateCard[];
}>();
</script>

<style scoped>
section {
	background-color: var(--text-color-subdued);

	color: var(--gray-0);
	min-width: 1.5rem;
	min-height: 1.5rem;
	border-radius: var(--border-radius);
	display: flex;
	justify-content: center;
	align-items: center;
	position: relative;

	&:hover {
		& > i {
			display: none;
		}
		& > ul {
			display: block;
		}
	}

	& > ul {
		display: none;
		position: absolute;
		left: 50%;
		top: 50%;
		transform: translate(-50%, -50%);
		border: 2px solid var(--text-color-subdued);
		border-radius: var(--border-radius);
		padding: var(--gap-2);
		list-style: none;
		background-color: var(--surface-section);
		color: var(--text-color-primary);
		font-size: var(--font-caption);

		& > li {
			text-wrap: nowrap;
		}
	}
}
</style>
