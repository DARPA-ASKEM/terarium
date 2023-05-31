<template>
	<main>
		<tera-slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="300px"
			header="Resources"
			direction="left"
			class="resource-panel"
		>
			<template v-slot:content>
				<tera-resource-sidebar
					:project="project"
					:tabs="tabs"
					:active-tab="openedAssetRoute"
					@open-asset="openAssetFromSidebar"
					@close-tab="removeClosedTab"
					@click="getAndPopulateAnnotations()"
					@remove-asset="removeAsset"
				/>
			</template>
		</tera-slider-panel>
		<Splitter>
			<SplitterPanel class="project-page" :size="20">
				<tera-tab-group
					v-if="!isEmpty(tabs)"
					class="tab-group"
					:tabs="tabs"
					:active-tab-index="activeTabIndex"
					:loading-tab-index="loadingTabIndex"
					@close-tab="removeClosedTab"
					@select-tab="openAsset"
					@click="getAndPopulateAnnotations()"
				/>
				<tera-project-page
					:project="project"
					:asset-id="assetId"
					:page-type="pageType"
					v-model:tabs="tabs"
					@asset-loaded="setActiveTab"
					@close-current-tab="removeClosedTab(activeTabIndex as number)"
					@update-project="updateProject"
				/>
			</SplitterPanel>
			<SplitterPanel
				class="project-page"
				v-if="
					pageType === ProjectAssetTypes.SIMULATION_WORKFLOW &&
					((openedWorkflowNodeStore.assetId && openedWorkflowNodeStore.pageType) ||
						openedWorkflowNodeStore.node)
				"
				:size="20"
			>
				<tera-tab-group
					v-if="openedWorkflowNodeStore.node"
					class="tab-group"
					:tabs="[{ assetName: openedWorkflowNodeStore.node.operationType }]"
					:active-tab-index="0"
					:loading-tab-index="null"
					@close-tab="openedWorkflowNodeStore.node = openedWorkflowNodeStore.assetId = null"
				/>
				<tera-calibration
					v-if="openedWorkflowNodeStore.node?.operationType === WorkflowOperationTypes.CALIBRATION"
					:node="openedWorkflowNodeStore.node"
				/>
				<tera-simulate
					v-if="openedWorkflowNodeStore.node?.operationType === WorkflowOperationTypes.SIMULATE"
					:node="openedWorkflowNodeStore.node"
				/>
				<tera-project-page
					v-else
					:project="project"
					:asset-id="openedWorkflowNodeStore.assetId ?? undefined"
					:page-type="openedWorkflowNodeStore.pageType ?? undefined"
					is-drilldown
					@asset-loaded="setActiveTab"
				/>
			</SplitterPanel>
		</Splitter>
		<tera-slider-panel
			class="slider"
			content-width="240px"
			direction="right"
			header="Notes"
			v-model:is-open="isNotesSliderOpen"
			@click="getAndPopulateAnnotations()"
		>
			<template v-slot:content>
				<section class="annotation-panel-container">
					<div v-for="(annotation, idx) of annotations" :key="idx">
						<div
							v-if="isEditingNote && idx === selectedNoteIndex"
							class="annotation-input-container"
						>
							<div class="annotation-header">
								<Dropdown
									placeholder="Unassigned"
									class="p-button p-button-text notes-dropdown-button"
									:options="noteOptions"
									v-model="selectedNoteSection[idx]"
								/>
							</div>
							<Textarea
								v-model="annotation.content"
								ref="annotationTextInput"
								rows="5"
								cols="30"
								aria-labelledby="annotation"
							/>
							<div class="save-cancel-buttons">
								<Button
									@click="isEditingNote = false"
									label="Cancel"
									class="p-button p-button-secondary"
									size="small"
								/>
								<Button
									@click="
										updateNote();
										isEditingNote = false;
									"
									label=" Save"
									class="p-button"
									size="small"
								/>
							</div>
						</div>
						<div v-else>
							<div class="annotation-header">
								<!-- TODO: Dropdown menu is for selecting which section to assign the note to: Unassigned, Abstract, Methods, etc. -->
								<Dropdown
									disabled
									placeholder="Unassigned"
									class="p-button p-button-text notes-dropdown-button"
									:options="noteOptions"
									v-model="selectedNoteSection[idx]"
								/>
								<!-- TODO: Ellipsis button should open a menu with options to: Edit note & Delete note -->
								<Button
									icon="pi pi-ellipsis-v"
									class="p-button-rounded p-button-secondary"
									@click="
										(event) => {
											toggleAnnotationMenu(event);
											selectedNoteIndex = idx;
										}
									"
								/>
							</div>
							<div>
								<p>{{ annotation.content }}</p>
								<div class="annotation-author-date">
									<div>
										{{ formatAuthorTimestamp(annotation.username, annotation.timestampMillis) }}
									</div>
								</div>
							</div>
						</div>
					</div>
					<Menu
						ref="annotationMenu"
						:model="annotationMenuItems"
						:popup="true"
						@hide="onHide"
						@click.stop
					/>
				</section>
				<div class="annotation-input-box">
					<div v-if="isAnnotationInputOpen" class="annotation-input-container">
						<div class="annotation-header">
							<Dropdown
								placeholder="Unassigned"
								class="p-button p-button-text notes-dropdown-button"
								:options="noteOptions"
								v-model="newNoteSection"
							/>
						</div>
						<Textarea
							v-model="annotationContent"
							ref="annotationTextInput"
							rows="5"
							cols="30"
							aria-labelledby="annotation"
						/>
						<div class="save-cancel-buttons">
							<Button
								@click="toggleAnnotationInput()"
								label="Cancel"
								class="p-button p-button-secondary"
								size="small"
							/>
							<Button
								@click="
									addNote();
									toggleAnnotationInput();
								"
								label=" Save"
								class="p-button"
								size="small"
							/>
						</div>
					</div>
					<div v-else>
						<Button
							@click="toggleAnnotationInput()"
							icon="pi pi-plus"
							label="Add note"
							class="p-button-text p-button-flat"
						/>
					</div>
				</div>
			</template>
		</tera-slider-panel>
	</main>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { isEmpty, isEqual } from 'lodash';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';
import { useTabStore } from '@/stores/tabs';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { Tab, Annotation } from '@/types/common';
import { IProject, ProjectAssetTypes, ProjectPages, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import { formatDdMmmYyyy, formatLocalTime, isDateToday } from '@/utils/date';
import {
	createAnnotation,
	deleteAnnotation,
	getAnnotations,
	updateAnnotation
} from '@/services/models/annotations';
import Menu from 'primevue/menu';
import Splitter from 'primevue/splitter';
import SplitterPanel from 'primevue/splitterpanel';
import TeraCalibration from '@/components/workflow/tera-calibration.vue';
import TeraSimulate from '@/components/workflow/tera-simulate.vue';
import { WorkflowOperationTypes } from '@/types/workflow';
import TeraProjectPage from './components/tera-project-page.vue';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
}>();

const emit = defineEmits(['update-project']);

const tabStore = useTabStore();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const router = useRouter();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');
const isAnnotationInputOpen = ref(false);
const annotationMenu = ref();
const menuOpenEvent = ref();
const selectedNoteIndex = ref();
const isEditingNote = ref(false);
const isNoteDeletionConfirmation = ref(false);
const noteDeletionConfirmationMenuItem = {
	label: 'Are you sure?',
	icon: '',
	items: [
		{
			label: 'Yes, delete this note',
			command: () => deleteNote()
		}
	]
};
const annotationMenuItems = ref([
	{
		label: 'Edit',
		icon: 'pi pi-fw pi-file-edit',
		command: () => {
			isEditingNote.value = true;
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash',
		command: () => {
			if (!isNoteDeletionConfirmation.value) {
				// @ts-ignore
				annotationMenuItems.value.push(noteDeletionConfirmationMenuItem);
				isNoteDeletionConfirmation.value = true;
			}
		}
	}
]);

function onHide() {
	if (isNoteDeletionConfirmation.value) {
		annotationMenu.value.show(menuOpenEvent.value);
		isNoteDeletionConfirmation.value = false;
	} else if (annotationMenuItems.value.length > 2) {
		annotationMenuItems.value.pop();
	}
}

const toggleAnnotationMenu = (event) => {
	// Fake object to allow the Menu component to not hide after clicking an item.
	// Actually I am immediately showing it again after it automatically hides.
	// I need to save and pass event.currentTarget in a ref to use when I want to manually show the menu.
	// Kind of a hack/workaround due to the restrictive nature of this component.
	menuOpenEvent.value = {
		currentTarget: event.currentTarget
	};
	annotationMenu.value.toggle(event);
};

enum NoteSection {
	Unassigned = 'Unassigned',
	Abstract = 'Abstract',
	Intro = 'Intro',
	Methods = 'Methods',
	Results = 'Results',
	Discussion = 'Discussion',
	References = 'References'
}
const noteOptions = ref([
	NoteSection.Unassigned,
	NoteSection.Abstract,
	NoteSection.Intro,
	NoteSection.Methods,
	NoteSection.Results,
	NoteSection.Discussion,
	NoteSection.References
]);
const selectedNoteSection = ref<string[]>([]);
const newNoteSection = ref();

// Associated with tab storage
const projectContext = computed(() => props.project?.id.toString());
const tabs = computed(() => tabStore.getTabs(projectContext.value) ?? []);
const activeTabIndex = ref<number | null>(0);
const openedAssetRoute = computed<Tab>(() => ({
	assetName: props.assetName ?? '',
	pageType: props.pageType,
	assetId: props.assetId
}));
const loadingTabIndex = ref<number | null>(null);

function setActiveTab() {
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);
	loadingTabIndex.value = null;
}

function updateProject(id: IProject['id']) {
	emit('update-project', id);
}

function openAsset(index: number = tabStore.getActiveTabIndex(projectContext.value)) {
	activeTabIndex.value = null;
	const asset: Tab = tabs.value[index];
	if (
		!(
			asset &&
			asset.assetId === props.assetId &&
			asset.assetName === props.assetName &&
			asset.pageType === props.pageType
		)
	) {
		loadingTabIndex.value = index;
		router.push({ name: RouteName.ProjectRoute, params: asset });
	}
}

function openAssetFromSidebar(asset: Tab = tabs.value[activeTabIndex.value!]) {
	router.push({ name: RouteName.ProjectRoute, params: asset });
	loadingTabIndex.value = tabs.value.length;
}

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(projectContext.value, tabIndexToRemove);
	activeTabIndex.value = tabStore.getActiveTabIndex(projectContext.value);
}

async function removeAsset(asset: Tab) {
	const { assetName, assetId, pageType } = asset;

	// Delete only Asset with an ID and of ProjectAssetType
	if (
		assetId &&
		pageType &&
		isProjectAssetTypes(pageType) &&
		pageType !== ProjectPages.OVERVIEW &&
		pageType !== ProjectAssetTypes.SIMULATION_WORKFLOW
	) {
		const isRemoved = await ProjectService.deleteAsset(props.project.id, pageType, assetId);

		if (isRemoved) {
			emit('update-project', props.project.id);
			removeClosedTab(tabs.value.findIndex((tab: Tab) => isEqual(tab, asset)));
			logger.info(`${assetName} was removed.`, { showToast: true });
			return;
		}
	}

	logger.error(`Failed to remove ${assetName}`, { showToast: true });
}

watch(
	() => projectContext.value,
	() => {
		if (projectContext.value) {
			// Automatically go to overview page when project is opened
			router.push({
				name: RouteName.ProjectRoute,
				params: { assetName: 'Overview', pageType: ProjectPages.OVERVIEW, assetId: undefined }
			});
		}
		if (
			tabs.value.length > 0 &&
			tabs.value.length >= tabStore.getActiveTabIndex(projectContext.value)
		) {
			openAsset();
		} else if (openedAssetRoute.value && openedAssetRoute.value.assetName) {
			tabStore.addTab(projectContext.value, openedAssetRoute.value);
		}
	}
);

watch(
	() => openedAssetRoute.value, // Once route attributes change, add/switch to another tab
	(newOpenedAssetRoute) => {
		if (newOpenedAssetRoute.assetName) {
			// If name isn't recognized, its a new asset so add a new tab
			if (
				props.assetName &&
				props.pageType &&
				!tabs.value.some((tab) => isEqual(tab, newOpenedAssetRoute))
			) {
				tabStore.addTab(projectContext.value, newOpenedAssetRoute);
			}
			// Tab switch
			else if (props.assetName) {
				const index = tabs.value.findIndex((tab) => isEqual(tab, newOpenedAssetRoute));
				tabStore.setActiveTabIndex(projectContext.value, index);
			}
			// Goes to tab from previous session
			else {
				openAsset(tabStore.getActiveTabIndex(projectContext.value));
			}
		}
	}
);

tabStore.$subscribe(() => {
	openAsset(tabStore.getActiveTabIndex(projectContext.value));
});

async function getAndPopulateAnnotations() {
	if (props.assetId && props.pageType) {
		annotations.value = await getAnnotations(props.assetId, props.pageType);
		selectedNoteSection.value = annotations.value?.map((note) => note.section);
	} else {
		selectedNoteSection.value = [];
	}
}

const addNote = async () => {
	await createAnnotation(
		newNoteSection.value,
		annotationContent.value,
		props.assetId,
		props.pageType
	);
	annotationContent.value = '';
	newNoteSection.value = NoteSection.Unassigned;
	await getAndPopulateAnnotations();
};

async function updateNote() {
	const noteToUpdate: Annotation = annotations.value[selectedNoteIndex.value];
	const section =
		selectedNoteSection.value.length >= selectedNoteIndex.value
			? selectedNoteSection.value[selectedNoteIndex.value]
			: null;
	await updateAnnotation(noteToUpdate.id, section, noteToUpdate.content);
	await getAndPopulateAnnotations();
}

async function deleteNote() {
	const noteToDelete: Annotation = annotations.value[selectedNoteIndex.value];
	await deleteAnnotation(noteToDelete.id);
	await getAndPopulateAnnotations();
}

function toggleAnnotationInput() {
	isAnnotationInputOpen.value = !isAnnotationInputOpen.value;
	if (isAnnotationInputOpen.value === false) {
		annotationContent.value = '';
	}
}

function formatAuthorTimestamp(username, timestamp) {
	if (isDateToday(timestamp)) {
		return `${username} at ${formatLocalTime(timestamp)} today`;
	}
	return `${username} on ${formatDdMmmYyyy(timestamp)}`;
}
</script>

<style scoped>
.resource-panel {
	z-index: 2;
	isolation: isolate;
}

.tab-group {
	z-index: 2;
	isolation: isolate;
	position: relative;
}

.p-splitter {
	display: flex;
	flex: 1;
	background: none;
	border: none;
}

section,
.p-splitter:deep(.project-page) {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow-x: auto;
	overflow-y: hidden;
}

.p-tabmenu:deep(.p-tabmenuitem) {
	display: inline;
	max-width: 15rem;
}

.p-tabmenu:deep(.p-tabmenu-nav .p-tabmenuitem .p-menuitem-link) {
	padding: 1rem;
	text-decoration: none;
}

.p-tabmenu:deep(.p-menuitem-text) {
	height: 1rem;
	display: inline-block;
	overflow: hidden;
	text-overflow: ellipsis;
}

.annotation-header {
	display: flex;
	justify-content: space-between;
}

.annotation-header .p-button.p-button-secondary {
	background-color: var(--surface-section);
}

.annotation-panel-container {
	display: flex;
	gap: 16px;
	padding: 0 16px 0 16px;
}

.notes-dropdown-button {
	color: var(--text-color-subdued);
	padding: 0rem;
}

.annotation-panel .p-dropdown:not(.p-disabled).p-focus {
	box-shadow: none;
	background-color: var(--surface-hover);
}

.annotation-author-date {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	display: flex;
	justify-content: space-between;
	gap: 0.5rem;
	padding-top: 0.25rem;
	padding-right: 1rem;
}

.annotation-content {
	padding: 0rem 0.5rem 0rem 0.5rem;
}

.annotation-input-container {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.annotation-input-box {
	padding: 16px 16px 8px 16px;
}

.annotation-input-box .p-inputtext {
	border-color: var(--surface-border);
	max-width: 100%;
	min-width: 100%;
}

.annotation-input-box .p-inputtext:hover {
	border-color: var(--primary-color) !important;
}

.annotation-header {
	height: 2rem;
}

.save-cancel-buttons {
	display: flex;
	gap: 0.5rem;
}

.save-cancel-buttons > button {
	flex: 1;
}
</style>
