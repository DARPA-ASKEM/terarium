<script setup lang="ts">
import ProjectCard from '@/components/projects/ProjectCard.vue';
import NewProjectCard from '@/components/projects/NewProjectCard.vue';
import ArticlesCard from '@/components/articles/ArticlesCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import { onMounted, ref } from 'vue';
import { Project } from '@/types/Project';
import { XDDArticle } from '@/types/XDD';
import * as ProjectService from '@/services/project';
import { searchXDDArticles } from '@/services/data';

const enum Categories {
	Recents = 'Recents',
	Trending = 'Trending',
	Epidemiology = 'Epidemiology'
}

const categories = new Map<string, { icon: object }>([[Categories.Recents, { icon: IconTime32 }]]);

const projects = ref<Project[]>([]);
const articles = ref<XDDArticle[]>([]);
const relevantSearchTerm = 'Covid 19';

onMounted(async () => {
	const allProjects = await ProjectService.getAll();
	if (allProjects) {
		projects.value = allProjects;
	}

	// Get all relevant articles
	// TODO: Add 2nd param so we can limit amount of results and maybe filter on just newest ect
	const allArticles = await searchXDDArticles(relevantSearchTerm);
	// console.log(allArticles);
	if (allArticles) {
		articles.value = allArticles.results;
	}
});
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
					<li v-for="(project, index) in projects" :key="index">
						<router-link
							style="text-decoration: none; color: inherit"
							:to="'/projects/' + project.id"
							:projectId="project.id"
						>
							<ProjectCard :name="project.name" />
						</router-link>
					</li>
				</ul>
				<IconChevronRight32 class="chevron-right" />
			</div>
			<!-- Hot Topics carousel -->
			<header>
				<h3>Latest on {{ relevantSearchTerm }}</h3>
			</header>
			<div class="project-carousel">
				<IconChevronLeft32 class="chevron-left" />
				<ul>
					<li v-for="(paper, index) in articles" :key="index">
						<!-- TODO: Rather than link to a separate page have a popup window for this -->
						<router-link
							style="text-decoration: none; color: inherit"
							:to="'/docs/' + paper.gddid"
							:paperId="paper.gddid"
						>
							<ArticlesCard :name="paper.title" />
						</router-link>
					</li>
				</ul>
			</div>

			<!-- For each project show related articles -->
			<!-- TODO: Only show this if there are related articles to begin with -->
			<li v-for="(project, index) in projects" :key="index">
				<header>
					<h3>Related to: {{ project.name }}</h3>
				</header>
				<div class="project-carousel">
					<IconChevronLeft32 class="chevron-left" />
					<ul>
						<li v-for="(paper, index) in project.relatedArticles" :key="index">
							<router-link
								style="text-decoration: none; color: inherit"
								:to="'/docs/' + paper.gddid"
								:paperId="paper.gddid"
							>
								<ArticlesCard :name="paper.title" />
							</router-link>
						</li>
					</ul>
				</div>
			</li>
		</template>
	</section>
</template>

<style scoped>
section {
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-body-text-secondary);
	display: flex;
	flex-direction: column;
	flex-grow: 1;
}

header {
	align-items: center;
	display: flex;
	z-index: -1;
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
	color: var(--un-color-accent);
	margin-right: 0.5rem;
}

.project-carousel {
	align-items: center;
	display: flex;
	z-index: -1;
}

.project-carousel svg {
	/* 	chevron arrows - see about making the right ones appear as required (by watching scrollbar position)
		eg. if I am on the very left of the carousel don't show the left arrow
	*/
	cursor: pointer;
	margin: 0 0.5rem 1rem 1rem;
	position: absolute;
	visibility: hidden;
	z-index: 2;
}

/* Hide the arrows for now */
.project-carousel svg[class|='chevron'] {
	display: none;
}

.chevron-right {
	right: 0;
}

.project-carousel svg:hover {
	background-color: var(--un-color-body-surface-background);
	border-radius: 10rem;
	color: var(--un-color-accent);
}

.project-carousel:hover svg {
	visibility: visible;
}

ul {
	align-items: center;
	display: flex;
	gap: 0.5rem;
	margin: 0.5rem 4rem;
}

li {
	height: 15rem;
	list-style: none;
	min-width: 20rem;
	position: relative;
}

a:hover {
	transform: scale(1.2);
	z-index: 2;
	transition: 0.2s;
}

li > * {
	position: absolute;
}

.project-carousel:last-of-type {
	margin-bottom: 3rem;
}
</style>
