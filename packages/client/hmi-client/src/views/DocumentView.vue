<script setup lang="ts">
import Document from '@/components/articles/Document.vue';
import TabContainer from '@/components/tabs/TabContainer.vue';
import { ref, watch, computed } from 'vue';
import { Tab, Annotation, ResourceType } from '@/types/common';
import useResourcesStore from '@/stores/resources';
import { Project } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { useTabStore } from '@/stores/tabs';
import { isEmpty } from 'lodash';
import ResourcesList from '@/components/resources/resources-list.vue';
import SliderPanel from '@/components/widgets/slider-panel.vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

import API from '@/api/api';

const props = defineProps<{
	assetId?: string;
	project: Project;
}>();

const resourcesStore = useResourcesStore();
const tabStore = useTabStore();

const isPanelOpen = ref(false);
const annotations = ref<Annotation[]>([]);

const annotationContent = ref<string>('');
const newDocumentId = computed(() => props.assetId);
const openTabs = ref<Tab[]>([]);
const activeTabIndex = ref(0);
const documentsInCurrentProject = resourcesStore.activeProjectAssets?.publications; // fixme
const activeProject = resourcesStore.activeProject;
const tabContext = `document${activeProject?.id}`;

// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
tabStore.$subscribe((mutation, state) => {
	const savedTabs = state.tabMap.get(tabContext);
	if (savedTabs) {
		openTabs.value = savedTabs;
	}
	activeTabIndex.value = tabStore.getActiveTabIndex(tabContext);
});

function removeClosedTab(tabIndexToRemove: number) {
	tabStore.removeTab(tabContext, tabIndexToRemove);
}

function getDocumentName(id: string): string | null {
	const currentDocument = documentsInCurrentProject?.find((doc) => doc.xdd_uri.toString() === id);
	if (currentDocument) {
		return currentDocument.title;
	}
	return null;
}

function setActiveTab(index: number) {
	activeTabIndex.value = index;
	tabStore.setActiveTabIndex(tabContext, index);
}

const fetchAnnotations = async () => {
	const response = await API.get('/annotations', {
		params: {
			artifact_type: ResourceType.XDD,
			artifact_id: newDocumentId.value
		}
	});
	annotations.value = response.data;
};

const addAnnotation = async () => {
	const content = annotationContent.value;
	await API.post('/annotations', {
		content,
		artifact_id: newDocumentId.value,
		artifact_type: ResourceType.XDD // FIXME: need to establish artifact types
	});

	// Refresh
	await fetchAnnotations();
};

watch(newDocumentId, (id) => {
	if (id) {
		const newTab = {
			name: getDocumentName(id),
			props: {
				assetId: id
			}
		} as Tab;
		// Would have loved to use a Set here instead of an array, but equality does not work as expected for objects
		const foundTabIndex = openTabs.value.findIndex((tab) => {
			const tabProps = tab.props as { assetId: string };
			return tabProps.assetId === props.assetId;
		});
		if (foundTabIndex === -1) {
			openTabs.value.push(newTab);
			tabStore.setTabs(tabContext, openTabs.value);
			tabStore.setActiveTabIndex(tabContext, openTabs.value.length - 1);
		} else {
			tabStore.setActiveTabIndex(tabContext, foundTabIndex);
		}
		fetchAnnotations();
	}
});

const previousOpenTabs = tabStore.getTabs(tabContext);
if (previousOpenTabs) {
	openTabs.value = openTabs.value.concat(previousOpenTabs);
	setActiveTab(tabStore.getActiveTabIndex(tabContext));
}

const formatDate = (millis: number) => {
	const dt = new Date(millis).toLocaleDateString();
	return dt;
};
</script>

<template>
	<TabContainer
		v-if="!isEmpty(Array.from(openTabs))"
		class="tab-container"
		:tabs="Array.from(openTabs)"
		:component-to-render="Document"
		:active-tab="props.assetId"
		@tab-closed="(tab) => removeClosedTab(tab)"
		@tab-selected="(index) => setActiveTab(index)"
		:active-tab-index="activeTabIndex"
	>
	</TabContainer>
	<section v-else class="recent-documents-page">
		<resources-list :project="props.project" :resource-route="RouteName.ModelRoute" />
	</section>

	<slider-panel
		class="slider"
		content-width="300px"
		tab-width="56px"
		direction="right"
		header="Notes"
		v-model:is-open="isPanelOpen"
	>
		<template v-slot:content>
			<div v-for="(annotation, idx) of annotations" :key="idx" class="annotation-panel">
				<p>{{ annotation.content }}</p>
				<div class="annotation-footer">
					<div>{{ annotation.username }}</div>
					<div>{{ formatDate(annotation.timestampMillis) }}</div>
				</div>
			</div>
			<div class="annotation-panel">
				<Textarea v-model="annotationContent" rows="5" cols="30" aria-labelledby="annotation" />
				<Button @click="addAnnotation()" label="Add note" />
			</div>
		</template>
	</slider-panel>
</template>

<style scoped>
.recent-documents-page {
	margin: 10px;
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	background: var(--surface-section);
	flex: 1;
}

.tab-container {
	height: 100%;
	flex: 1;
}

.slider {
	background: var(--surface-card);
}

.annotation-panel {
	padding: 8px;
	font-size: var(--font-body-small);
}

.annotation-footer {
	text-align: right;
}
</style>
