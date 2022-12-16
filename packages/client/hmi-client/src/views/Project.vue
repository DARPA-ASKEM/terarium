<script setup lang="ts">
import { Project } from '@/types/Project';
import ResourcesList from '@/components/resources/resources-list.vue';

const props = defineProps<{
	project: Project;
}>();

const emit = defineEmits(['show-data-explorer']);
</script>

<template>
	<div class="flex-container">
		<header>
			<h2>{{ project?.name }}</h2>
			<p class="secondary-text">Last updated: {{ project?.timestamp }}</p>
		</header>
		<section class="content-container">
			<section class="summary">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<section class="description">
						<!-- Author -->
						<section class="author">Edwin Lai, Yohann Paris</section>
						<p>
							{{ project?.description }}
						</p>
					</section>
				</div>
			</section>
			<section class="detail">
				<resources-list
					:project="props?.project"
					@show-data-explorer="emit('show-data-explorer')"
				/>
			</section>
		</section>
	</div>
</template>

<style scoped>
.flex-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	margin: 0.5rem;
	background: white;
}

a {
	text-decoration: underline;
}

.content-container {
	margin-left: 1rem;
	column-gap: 2rem;
	flex-direction: row;
}

header {
	margin: 1rem;
}

section {
	display: flex;
	width: 100%;
	flex-direction: column;
}

.summary {
	flex: 0.25;
}

.author {
	line-height: 1.75rem;
}

.detail {
	flex: 0.75;
}

.summary section,
.detail section {
	margin: 1rem 0;
	display: block;
}

h4 {
	font: var(--un-font-h4);
	margin: 1rem 0;
}

h3 {
	font: var(--un-font-h3);
}

h2 {
	font: var(--un-font-h2);
}

.secondary-text {
	color: var(--un-color-body-text-secondary);
}
</style>
