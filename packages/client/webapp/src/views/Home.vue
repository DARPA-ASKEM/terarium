<script setup lang="ts">
import ProjectCard from '@/components/projects/ProjectCard.vue';
import NewProjectCard from '@/components/projects/NewProjectCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconFire32 from '@carbon/icons-vue/es/fire/32';
import IconCoronavirus32 from '@carbon/icons-vue/es/coronavirus/32';

const enum Categories {
	Recents = 'Recents',
	Trending = 'Trending',
	Epidemiology = 'Epidemiology'
}

const categories = new Map<string, { icon: object }>([
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
			<header>
				<component :is="value.icon" />
				<h3>{{ key }}</h3>
			</header>
			<div class="project-carousel">
				<IconChevronLeft32 class="chevron-left" />
				<ul>
					<li v-if="key === Categories.Recents">
						<NewProjectCard />
					</li>
					<li v-for="(project, index) in dummyProjects" :key="index">
						<ProjectCard :name="project" />
					</li>
				</ul>
				<IconChevronRight32 class="chevron-right" />
			</div>
		</template>
	</section>
</template>

<style scoped>
section {
	color: var(--un-color-body-text-secondary);
	background-color: var(--un-color-body-surface-secondary);
	margin-bottom: 10rem;
}

header {
	display: flex;
	align-items: center;
}

h2 {
	font: var(--un-font-h2);
}

h3 {
	font: var(--un-font-h3);
}

h2,
header {
	margin-left: 4rem;
	margin-top: 1rem;
}

header svg {
	margin-right: 0.5rem;
	color: var(--un-color-accent);
}

.project-carousel {
	display: flex;
	align-items: center;
	/* width: 100vw; */
	/* overflow-y: auto; */
}

.project-carousel svg {
	/** chevron arrows - see about making the right ones appear as required (by watching scrollbar position)
		eg. if I am on the very left of the carousel don't show the left arrow
	*/
	position: absolute;
	margin: 0 0.5rem 1rem 1rem;
	cursor: pointer;
	z-index: 2;
	visibility: hidden;
}

.chevron-right {
	right: 0;
}

.project-carousel svg:hover {
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-accent);
	border-radius: 10rem;
}

.project-carousel:hover svg {
	visibility: visible;
}

ul {
	display: flex;
	padding: 0 4rem;
	margin: 0.5rem 0;
	transition: 0.2s;
	align-items: center;
	/* overflow: auto;
	width: 100vw; */
	gap: 0.5rem;
}

li {
	position: relative;
	height: 15rem;
	min-width: 20rem;
	list-style: none;
}

li:hover > .project-card {
	transform: scale(1.2);
	z-index: 2;
}

li > * {
	position: absolute;
}
</style>
