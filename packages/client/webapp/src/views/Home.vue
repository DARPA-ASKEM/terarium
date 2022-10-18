<script setup lang="ts">
import ProjectCard from '@/components/projects/ProjectCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconFire32 from '@carbon/icons-vue/es/fire/32';
import IconCoronavirus32 from '@carbon/icons-vue/es/coronavirus/32';
import NewProjectCard from '../components/projects/NewProjectCard.vue';

const enum Categories {
	Recents = 'Recents',
	Trending = 'Trending',
	Epidemiology = 'Epidemiology'
}

const categories = new Map<string, { icon: string }>([
	[Categories.Recents, { icon: IconTime32 }],
	[Categories.Trending, { icon: IconFire32 }],
	[Categories.Epidemiology, { icon: IconCoronavirus32 }]
]);

const dummyProjects: string[] = Array(8)
	.fill('Project')
	.map((project, index) => `${project} ${index}`);
</script>

<template>
	<section>
		<h2>Projects</h2>
		<template v-for="[key, value] in categories" :key="key">
			<h3><component :is="value.icon" />{{ key }}</h3>
			<div class="project-carousel">
				<IconChevronLeft32 />
				<ul>
					<NewProjectCard v-if="key === Categories.Recents" />
					<ProjectCard v-for="(project, index) in dummyProjects" :key="index" :name="project" />
				</ul>
				<IconChevronRight32 />
			</div>
		</template>
	</section>
</template>

<style scoped>
section {
	color: var(--un-color-body-text-secondary);
	padding-top: 1rem;
}

h2 {
	font: var(--un-font-h2);
}

h3 {
	font: var(--un-font-h3);
}

h2,
h3 {
	margin-left: 4rem;
}

h3 svg {
	margin-right: 0.5rem;
	color: var(--un-color-accent);
}

.project-carousel {
	display: flex;
	align-items: center;
}

.project-carousel svg {
	min-width: 32px;
	margin: 0.5rem;
	margin-bottom: 1rem;
	cursor: pointer;
	visibility: hidden;
}

.project-carousel svg:hover {
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-accent);
	border-radius: 10rem;
}

.project-carousel:hover .project-carousel {
	height: 22rem;
}

.project-carousel:hover svg {
	visibility: visible;
}

ul {
	display: flex;
	height: 18rem;
	transition: 0.2s;
	align-items: center;
	overflow-x: auto;
	overflow-y: hidden;
}
</style>
