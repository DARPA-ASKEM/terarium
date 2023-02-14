<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import DocumentsCard from '@/components/documents/DocumentsCard.vue';
import SelectedDocumentPane from '@/components/documents/selected-document-pane.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconClose32 from '@carbon/icons-vue/es/close/16';
import { Project } from '@/types/Project';
import { XDDSearchParams } from '@/types/XDD';
import { DocumentType } from '@/types/Document';
import { searchXDDDocuments } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import useQueryStore from '@/stores/query';
import API from '@/api/api';
import ProjectCard from '@/components/projects/ProjectCard.vue';

const projects = ref<Project[]>([]);
// Only display projects with at least one related document
// Only display at most 5 projects
const projectsToDisplay = computed(() =>
	projects.value.filter((project) => project.relatedDocuments !== undefined).slice(0, 5)
);
const relevantDocuments = ref<DocumentType[]>([]);
const relevantSearchTerm = 'COVID-19';
const relevantSearchParams: XDDSearchParams = { perPage: 15 }; // , fields: "abstract,title" };
const selectedDocument = ref<DocumentType>();

const resourcesStore = useResourcesStore();
const queryStore = useQueryStore();

onMounted(async () => {
	// Clear all...
	resourcesStore.reset(); // Project related resources saved.
	queryStore.reset(); // Facets queries.

	projects.value = (await API.get('/home')).data as Project[];

	// Get all relevant documents (latest on section)
	const allDocuments = await searchXDDDocuments(relevantSearchTerm, relevantSearchParams);
	if (allDocuments) {
		relevantDocuments.value = allDocuments.results;
	}
});

const selectDocument = (item: DocumentType) => {
	const itemID = item as DocumentType;
	selectedDocument.value = itemID;
};

const close = () => {
	selectedDocument.value = undefined;
};

const SCROLL_INCREMENT_IN_REM = 21;
const scroll = (direction: 'right' | 'left', event: PointerEvent) => {
	const chevronElement = event.target as HTMLElement;
	const cardListElement =
		chevronElement.nodeName === 'svg'
			? chevronElement.parentElement?.querySelector('ul')
			: chevronElement.parentElement?.parentElement?.querySelector('ul');

	if (cardListElement === null || cardListElement === undefined) return;

	// Don't scroll if last element is already within viewport
	if (direction === 'right' && cardListElement.lastElementChild) {
		const parentBounds = cardListElement.parentElement?.getBoundingClientRect();
		const bounds = cardListElement.lastElementChild.getBoundingClientRect();
		if (bounds && parentBounds && bounds.x + bounds.width < parentBounds.x + parentBounds.width) {
			return;
		}
	}

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
		<!-- modal window for showing selected document -->
		<div
			v-if="selectedDocument !== undefined"
			class="selected-document-modal-mask"
			@click="close()"
		>
			<div class="selected-document-modal" @click.stop>
				<div class="modal-header">
					<h4>{{ selectedDocument.title }}</h4>
					<IconClose32 class="close-button" @click="close()" />
				</div>
				<selected-document-pane
					class="selected-document-pane"
					:selected-document="selectedDocument"
					@close="close()"
				/>
			</div>
		</div>

		<h4>Projects</h4>
		<div class="carousel">
			<header>
				<component :is="IconTime32" />
				<h5>Recent</h5>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<!-- .slice() to copy the array, .reverse() to put new projects at the left of the list instead of the right -->
				<li v-for="(project, index) in projects.slice().reverse()" class="card" :key="index">
					<router-link
						style="text-decoration: none; color: inherit"
						:to="'/projects/' + project.id"
						:projectId="project.id"
					>
						<ProjectCard :project="project"></ProjectCard>
					</router-link>
				</li>
			</ul>
		</div>
		<!-- Hot Topics carousel -->
		<div class="carousel" v-if="relevantDocuments.length > 0">
			<header>
				<h5>Latest on {{ relevantSearchTerm }}</h5>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<li v-for="(document, index) in relevantDocuments" :key="index" class="card">
					<DocumentsCard :document="document" @click="selectDocument(document)" />
				</li>
			</ul>
		</div>
		<!-- Show related documents for the top 5 projects -->
		<div v-for="(project, index) in projectsToDisplay" :key="index" class="carousel">
			<header>
				<h5>Related to: {{ project.name }}</h5>
			</header>
			<IconChevronLeft32 class="chevron chevron-left" @click="scroll('left', $event)" />
			<IconChevronRight32 class="chevron chevron-right" @click="scroll('right', $event)" />
			<ul>
				<li v-for="(document, j) in project.relatedDocuments" :key="j" class="card">
					<DocumentsCard :document="document" @click="selectDocument(document)" />
				</li>
			</ul>
		</div>
	</section>
</template>

<style scoped>
section {
	background-color: var(--surface-secondary);
	color: var(--text-color-secondary);
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

h4 {
	margin-left: 4rem;
}

h4,
header {
	margin-top: 1rem;
}

header svg {
	color: var(--primary-color);
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
	background: var(--surface-secondary);
	border-radius: 10rem;
}

.chevron:hover {
	background-color: var(--surface-ground);
	color: var(--primary-color);
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
	margin-right: 1rem;
}

.card {
	z-index: 1;
	transition: 0.2s;
	max-width: 21rem;
	/* See SCROLL_INCREMENT_IN_REM */
}

.carousel:last-of-type {
	margin-bottom: 3rem;
}

.selected-document-modal-mask {
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

.selected-document-modal {
	position: relative;
	width: 500px;
	margin: 0px auto;
	background-color: var(--surface-section);
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

.selected-document-pane {
	margin: 2rem 0;
}
</style>
