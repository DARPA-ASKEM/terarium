<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ProjectCard from '@/components/projects/ProjectCard.vue';
import NewProjectCard from '@/components/projects/NewProjectCard.vue';
import ArticlesCard from '@/components/articles/ArticlesCard.vue';
import SelectedArticlePane from '@/components/articles/selected-article-pane.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconClose32 from '@carbon/icons-vue/es/close/16';
import { Project } from '@/types/Project';
import { XDDArticle, XDDSearchParams } from '@/types/XDD';
import * as ProjectService from '@/services/project';
import { searchXDDArticles } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import useQueryStore from '@/stores/query';

const projects = ref<Project[]>([]);
// Only display projects with at least one related article
// Only display at most 5 projects
const projectsToDisplay = computed(() =>
	projects.value.filter((project) => project.relatedArticles.length > 0).slice(0, 5)
);
const relevantArticles = ref<XDDArticle[]>([]);
const relevantSearchTerm = 'COVID-19';
const relevantSearchParams: XDDSearchParams = { perPage: 30 };
const selectedPaper = ref<XDDArticle>();

const resourcesStore = useResourcesStore();
const queryStore = useQueryStore();

onMounted(async () => {
	// Clear all...
	resourcesStore.reset(); // Project related resources saved.
	queryStore.reset(); // Facets queries.

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

const SCROLL_INCREMENT_IN_REM = 21;
const scroll = (direction: 'right' | 'left', event: PointerEvent) => {
	const chevronElement = event.target as HTMLElement;
	const cardListElement = chevronElement.parentElement?.querySelector('ul');
	if (cardListElement === null || cardListElement === undefined) return;
	const marginLeftString =
		cardListElement.style.marginLeft === '' ? '0' : cardListElement.style.marginLeft;
	const currentMarginLeft = parseInt(marginLeftString, 10);
	const changeInRem = direction === 'right' ? -SCROLL_INCREMENT_IN_REM : SCROLL_INCREMENT_IN_REM;
	const newMarginLeft = currentMarginLeft + changeInRem;
	// Don't let the list scroll far enough left that we see space before the
	//	first card.
	cardListElement.style.marginLeft = `${newMarginLeft > 0 ? 0 : newMarginLeft}rem`;
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
		<div class="carousel">
			<header>
				<component :is="IconTime32" />
				<h3>Recent</h3>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<li class="card">
					<NewProjectCard />
				</li>
				<!-- .slice() to copy the array, .reverse() to put new projects at the left of the list instead of the right -->
				<li v-for="(project, index) in projects.slice().reverse()" class="card" :key="index">
					<router-link
						style="text-decoration: none; color: inherit"
						:to="'/projects/' + project.id"
						:projectId="project.id"
					>
						<ProjectCard :name="project.name" />
					</router-link>
				</li>
			</ul>
		</div>
		<!-- Hot Topics carousel -->
		<div class="carousel" v-if="relevantArticles.length > 0">
			<header>
				<h3>Latest on {{ relevantSearchTerm }}</h3>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<li v-for="(paper, index) in relevantArticles" :key="index" class="card">
					<ArticlesCard :article="paper" @click="selectArticle(paper)" />
				</li>
			</ul>
		</div>
		<!-- Show related articles for the top 5 projects -->
		<div v-for="(project, index) in projectsToDisplay" :key="index" class="carousel">
			<header>
				<h3>Related to: {{ project.name }}</h3>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<li v-for="(paper, j) in project.relatedArticles" :key="j" class="card">
					<ArticlesCard :article="paper" @click="selectArticle(paper)" />
				</li>
			</ul>
		</div>
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
	margin-left: 4rem;
}

h3 {
	font: var(--un-font-h3);
}

h2,
header {
	margin-top: 1rem;
}

header svg {
	color: var(--un-color-accent);
	margin-right: 0.5rem;
}

.carousel {
	position: relative;
	margin-left: 4rem;
}

.carousel ul {
	align-items: center;
	display: flex;
	margin: 0.5rem 0;
}

.chevron {
	/* 	see about making the right ones appear as required (by watching scrollbar position)
		eg. if I am on the very left of the carousel don't show the left arrow
	*/
	cursor: pointer;
	margin: 0 1rem;
	position: absolute;
	top: 50%;
	visibility: visible;
	z-index: 3;
	background: var(--un-color-body-surface-secondary);
	border-radius: 10rem;
}

.chevron:hover {
	background-color: var(--un-color-body-surface-background);
	color: var(--un-color-accent);
}

.chevron-left {
	left: -4rem;
}

.chevron-right {
	right: 0rem;
}

ul {
	align-items: center;
	display: inline-flex;
	gap: 0.5rem;
	transition: margin-left 0.2s;
}

li {
	list-style: none;
}

.card {
	z-index: 1;
	transition: 0.2s;
}

.card:hover {
	transform: scale(1.2);
	z-index: 2;
}

.carousel:last-of-type {
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
