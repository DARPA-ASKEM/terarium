<script setup lang="ts">
import ProjectCard from '@/components/projects/ProjectCard.vue';
import NewProjectCard from '@/components/projects/NewProjectCard.vue';
import ArticlesCard from '@/components/articles/ArticlesCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconClose32 from '@carbon/icons-vue/es/close/16';
import { onMounted, ref } from 'vue';
import { Project } from '@/types/Project';
import { XDDArticle, XDDSearchParams } from '@/types/XDD';
import * as ProjectService from '@/services/project';
import { searchXDDArticles } from '@/services/data';
import selectedArticlePane from '@/components/articles/selected-article-pane.vue';

const enum Categories {
	Recents = 'Recents',
	Trending = 'Trending',
	Epidemiology = 'Epidemiology'
}

const categories = new Map<string, { icon: object }>([[Categories.Recents, { icon: IconTime32 }]]);

const projects = ref<Project[]>([]);
const relevantArticles = ref<XDDArticle[]>([]);
const relevantSearchTerm = 'COVID-19';
const relevantSearchParams: XDDSearchParams = { perPage: 30 };
const selectedPaper = ref<XDDArticle>();

onMounted(async () => {
	const allProjects = (await ProjectService.getAll()) as Project[];
	if (allProjects) {
		// TODO: Fix this so we send backend all of this with 1 call and it deals with it all
		const promises = allProjects.map((project) => ProjectService.getRelatedArticles(project));
		const result = await Promise.all(promises);
		for (let i = 0; i < allProjects.length; i++) {
			allProjects[i].relatedArticles = result[i];
		}
		projects.value = await allProjects;
	}

	// Get all relevant articles (latest on section)
	const allArticles = await searchXDDArticles(relevantSearchTerm, relevantSearchParams);
	if (allArticles) {
		relevantArticles.value = allArticles.results;
	}
});

const selectArticle = (item: XDDArticle) => {
	const itemID = item as XDDArticle;
	selectedPaper.value = itemID;
};

const close = () => {
	selectedPaper.value = undefined;
};
</script>

<template>
	<section>
		<!-- modal window for showing selected paper -->
		<div v-if="selectedPaper !== undefined" class="selected-paper-modal-mask" @click="close()">
			<div class="selected-paper-modal" @click.stop>
				<div class="modal-header">
					<h4>{{ selectedPaper.title }}</h4>
					<IconClose32 class="close-button" @click="close()" />
				</div>
				<selected-article-pane
					class="selected-article-pane"
					:selected-article="selectedPaper"
					@close="close()"
				/>
			</div>
		</div>

		<h2>Projects</h2>
		<template v-for="[key, value] in categories" :key="key">
			<div>
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
			</div>
			<!-- Hot Topics carousel -->
			<div>
				<header>
					<h3>Latest on {{ relevantSearchTerm }}</h3>
				</header>
				<div class="project-carousel">
					<ul>
						<li v-for="(paper, index) in relevantArticles" :key="index">
							<div @click="selectArticle(paper)">
								<ArticlesCard :article="paper" />
							</div>
						</li>
					</ul>
				</div>
			</div>
			<!-- For the top 5 projects show related articles -->
			<!-- TODO: Only show this if there are related articles to begin with -->
			<div v-for="(project, index) in projects.slice(0, 5)" :key="index">
				<header>
					<h3>Related to: {{ project.name }}</h3>
				</header>
				<div class="project-carousel">
					<ul>
						<li v-for="(paper, j) in project.relatedArticles" :key="j">
							<div @click="selectArticle(paper)">
								<ArticlesCard :article="paper" />
							</div>
						</li>
					</ul>
				</div>
			</div>
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
	overflow-y: scroll;
	overflow-x: hidden;
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
	overflow-x: auto;
	overflow-y: hidden;
}

.project-carousel svg {
	/* 	chevron arrows - see about making the right ones appear as required (by watching scrollbar position)
		eg. if I am on the very left of the carousel don't show the left arrow
	*/
	cursor: pointer;
	margin: 0 0.5rem 1rem 1rem;
	position: absolute;
	visibility: visible;
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
	display: inline-flex;
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

.selected-paper-modal-mask {
	position: fixed;
	z-index: 9998;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
}
.selected-paper-modal {
	position: relative;
	width: 500px;
	margin: 0px auto;
	background-color: var(--un-color-body-surface-primary);
	border-radius: 2px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	overflow-y: auto;
	max-height: 75%;
	padding: 1rem;
}

.modal-header {
	display: flex;
	justify-content: space-between;
}

.close-button {
	width: 2rem;
	height: 2rem;
	cursor: pointer;
	opacity: 50%;
}

.close-button:hover {
	opacity: 100%;
}

.selected-article-pane {
	margin: 2rem 0;
}
</style>
