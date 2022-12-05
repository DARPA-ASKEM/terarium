<script setup lang="ts">
import ProjectCard from '@/components/projects/ProjectCard.vue';
import NewProjectCard from '@/components/projects/NewProjectCard.vue';
import ArticlesCard from '@/components/articles/ArticlesCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
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
const relevantSearchTerm = 'Covid 19';
const relevantSearchParams: XDDSearchParams = { perPage: 15 };
const selectedPaper = ref<XDDArticle>();

onMounted(async () => {
	const allProjects = (await ProjectService.getAll()) as Project[];
	if (allProjects) {
		for (let i = 0; i < allProjects.length; i++) {
			// TODO: needs await but its in for loop so i cannot commit
			// Not sure how else to go from promise<XDDArticles[]> to XDDArticles[]
			allProjects[i].relatedArticles = ProjectService.getRelatedArticles(allProjects[i]);
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
		<h2>Projects</h2>
		<!-- modal window for showing selected paper -->
		<div v-if="selectedPaper !== undefined" class="modal-mask-paper">
			<div class="modal-wrapper-paper" @click.stop="selectedPaper ? {} : close()">
				<div class="modal-container-paper">
					<div class="modal-header">
						<button type="button" class="close-button" @click="close()">Close</button>
						{{ selectedPaper.title }}
					</div>
					<div class="modal-body-paper">
						<selected-article-pane
							class="selected-resources-pane"
							:selected-search-items="[selectedPaper]"
							@close="close()"
						/>
					</div>
				</div>
			</div>
		</div>

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

.modal-mask-paper {
	position: fixed;
	z-index: 9998;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: table;
	transition: opacity 0.3s ease;
	overflow-y: auto;
}
.modal-wrapper-paper {
	display: table-cell;
	vertical-align: middle;
	height: 100%;
}
.modal-container-paper {
	position: relative;
	width: 500px;
	margin: 0px auto;
	background-color: #fff;
	border-radius: 2px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	transition: all 0.3s ease;
	overflow-y: auto;
	height: 75%;
}

.modal-header-paper {
	padding: 15px;
}
.modal-header {
	max-width: 90%; /* Leave room for close button */
}

.modal-body-paper {
	padding: 15px;
}
.close-button {
	position: absolute;
	right: 0;
}
</style>
