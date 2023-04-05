<template>
	<main>
		<tera-slider-panel
			v-model:is-open="isResourcesSliderOpen"
			content-width="300px"
			header="Resources"
			direction="left"
		>
			<template v-slot:content>
				<tera-resource-sidebar
					:project="project"
					:tabs="tabs"
					:active-tab="openedAssetRoute"
					@open-asset="openAsset"
					@open-overview="openOverview"
					@close-tab="removeClosedTab"
					@click="getAndPopulateAnnotations()"
					@remove-asset="removeAsset"
				/>
			</template>
		</tera-slider-panel>
		<section>
			<tera-tab-group
				v-if="!isEmpty(tabs)"
				:tabs="tabs"
				:active-tab-index="activeTabIndex"
				@close-tab="removeClosedTab"
				@select-tab="openAsset"
				@click="getAndPopulateAnnotations()"
			/>
			<template v-if="assetId && !isEmpty(tabs)">
				<document
					v-if="assetType === ProjectAssetTypes.DOCUMENTS"
					:xdd-uri="getXDDuri(assetId)"
					:previewLineLimit="10"
					:project="project"
					is-editable
					@open-asset="openAsset"
				/>
				<dataset
					v-else-if="assetType === ProjectAssetTypes.DATASETS"
					:asset-id="assetId"
					:project="project"
					is-editable
				/>
				<model
					v-else-if="assetType === ProjectAssetTypes.MODELS"
					:asset-id="assetId"
					:project="project"
					is-editable
				/>
				<simulation-plan
					v-else-if="assetType === ProjectAssetTypes.PLANS"
					:asset-id="assetId"
					:project="project"
				/>
				<simulation-run
					v-else-if="assetType === ProjectAssetTypes.SIMULATION_RUNS"
					:asset-id="assetId"
					:project="project"
				/>
			</template>
			<code-editor
				v-else-if="assetType === ProjectAssetTypes.CODE"
				:initial-code="code"
				@on-model-created="openNewModelFromCode"
			/>
			<tera-project-overview v-else-if="assetType === 'overview'" :project="project" />
			<section v-else class="no-open-tabs">
				<img src="@assets/svg/seed.svg" alt="Seed" />
				<p>You can open resources from the resource panel.</p>
				<Button label="Open project overview" @click="openOverview" />
			</section>
		</section>
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
									label="Save"
									class="p-button"
									size="small"
								/>
							</div>
						</div>
						<div v-else>
							<div class="annotation-header">
								<!-- TODO: Dropdown menu is for selecting which section to assign the note to: Unassigned, Abstract, Methods, etc. -->
								<Dropdown
									placeholder="Unassigned"
									class="p-button p-button-text notes-dropdown-button"
									:options="noteOptions"
									optionLabel="name"
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
								label="Save"
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
import Dataset from '@/components/dataset/Dataset.vue';
import Document from '@/components/documents/Document.vue';
import Model from '@/components/models/Model.vue';
import TeraSliderPanel from '@/components/widgets/tera-slider-panel.vue';
import TeraTabGroup from '@/components/widgets/tera-tab-group.vue';
import CodeEditor from '@/page/project/components/code-editor.vue';
import SimulationPlan from '@/page/project/components/Simulation.vue';
import TeraResourceSidebar from '@/page/project/components/tera-resource-sidebar.vue';
import TeraProjectOverview from '@/page/project/components/tera-project-overview.vue';
import { RouteName } from '@/router/routes';
import { getModel } from '@/services/model';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import { useTabStore } from '@/stores/tabs';
import SimulationRun from '@/temp/SimulationResult2.vue';
import { Tab, Annotation } from '@/types/common';
import { IProject, ProjectAssetTypes, isProjectAssetTypes } from '@/types/Project';
import { logger } from '@/utils/logger';
import { formatDdMmmYyyy, formatLocalTime, isDateToday } from '@/utils/date';
import { createAnnotation, getAnnotations } from '@/services/models/annotations';
import Menu from 'primevue/menu';

// Asset props are extracted from route
const props = defineProps<{
	project: IProject;
	assetName?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes | 'overview' | '';
}>();

const emit = defineEmits(['update-project']);

const tabStore = useTabStore();
const router = useRouter();
const resources = useResourcesStore();

const isResourcesSliderOpen = ref(true);
const isNotesSliderOpen = ref(false);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');
const code = ref<string>();
const isAnnotationInputOpen = ref(false);
const annotationMenu = ref();
const menuOpenEvent = ref();

const selectedNoteIndex = ref();
const isEditingNote = ref(false);
const isNoteDeletionConfirmation = ref(false);
const noteDeletionConfirmationMenuItem = {
	label: 'Are you sure?',
	items: [
		{
			label: 'Yes, delete this note'
		}
	]
};
const annotationMenuItems = ref([
	{
		label: 'Edit',
		icon: 'pi pi-fw pi-file-edit'
		// command: () => isEditingNote.value = true
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash',
		command: () => {
			if (!isNoteDeletionConfirmation.value) {
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
const noteOptions = ref([{ name: 'Test' }]);

// Associated with tab storage
const projectContext = computed(() => props.project?.id.toString());
const tabs = computed(() => tabStore.getTabs(projectContext.value) ?? []);
const activeTabIndex = computed(() => tabStore.getActiveTabIndex(projectContext.value));
const openedAssetRoute = computed<Tab>(() => ({
	assetName: props.assetName ?? '',
	assetType: props.assetType,
	assetId: props.assetId
}));

const getXDDuri = (assetId: Tab['assetId']): string =>
	ProjectService.getDocumentAssetXddUri(props?.project, assetId) ?? '';

function openAsset(asset: Tab = tabs.value[activeTabIndex.value], newCode?: string) {
	router.push({ name: RouteName.ProjectRoute, params: asset });

	if (newCode) {
		code.value = newCode;
		// addCreatedAsset(assetToOpen);
	}
}
const openOverview = () =>
	openAsset({ assetName: 'Overview', assetType: 'overview', assetId: undefined });

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(projectContext.value, tabIndexToRemove);
}

async function openNewModelFromCode(modelId, modelName) {
	await ProjectService.addAsset(props.project.id, ProjectAssetTypes.MODELS, modelId);
	const model = await getModel(modelId);
	if (model) {
		resources.activeProjectAssets?.[ProjectAssetTypes.MODELS].push(model);
	} else {
		logger.warn('Could not add new model to project.');
	}

	router.push({
		name: RouteName.ProjectRoute,
		params: {
			assetName: modelName,
			assetId: modelId,
			assetType: ProjectAssetTypes.MODELS
		}
	});
}

async function removeAsset(asset: Tab) {
	const { assetName, assetId, assetType } = asset;

	// Delete only Asset with an ID and of ProjectAssetType
	if (assetId && assetType && isProjectAssetTypes(assetType) && assetType !== 'overview') {
		const isRemoved = await ProjectService.deleteAsset(props.project.id, assetType, assetId);

		if (isRemoved) {
			emit('update-project', props.project.id);
			removeClosedTab(tabs.value.findIndex((tab: Tab) => isEqual(tab, asset)));
			logger.info(`${assetName} was removed.`, { showToast: true });
			return;
		}
	}

	logger.error(`Failed to remove ${assetName}`, { showToast: true });
}

// When a new tab is chosen, reflect that by opening its associated route
tabStore.$subscribe(() => openAsset());

watch(
	() => [
		openedAssetRoute.value, // Once route attributes change, add/switch to another tab
		projectContext.value // Make sure we are in the proper project context before opening assets
	],
	() => {
		if (projectContext.value) {
			// If name isn't recognized, its a new asset so add a new tab
			if (
				props.assetName &&
				props.assetType &&
				!tabs.value.some((tab) => isEqual(tab, openedAssetRoute.value))
			) {
				tabStore.addTab(projectContext.value, openedAssetRoute.value);
			}
			// Tab switch
			else if (props.assetName) {
				const index = tabs.value.findIndex((tab) => isEqual(tab, openedAssetRoute.value));
				tabStore.setActiveTabIndex(projectContext.value, index);
			}
			// Goes to tab from previous session
			else openAsset();
		}
	}
);

async function getAndPopulateAnnotations() {
	annotations.value = await getAnnotations(props.assetId);
}

const addNote = async () => {
	await createAnnotation(annotationContent.value, props.assetId);
	annotationContent.value = '';
	await getAndPopulateAnnotations();
};

const updateNote = async () => {};

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
section {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: auto;
	padding: 0.5rem 0.5rem 0;
}

.no-open-tabs {
	justify-content: center;
	gap: 2rem;
	margin-bottom: 8rem;
	align-items: center;
	color: var(--text-color-subdued);
}

.asset {
	padding-top: 1rem;
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

:deep(span.p-dropdown-label.p-placeholder) {
	color: var(--text-color-subdued);
}

:deep(span.p-dropdown-label) {
	padding: 0;
	font-size: var(--font-caption);
}

:deep(span.p-dropdown-trigger-icon.pi.pi-chevron-down) {
	color: var(--text-color-subdued);
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
