<template>
	<main @scroll="updateScrollPosition" id="tango" ref="assetContainer">
		<slot name="nav" />
		<header v-if="shrinkHeader || showStickyHeader" class="shrinked">
			<h4 v-html="name" />
			<aside class="spread-out">
				<slot name="edit-buttons" />
				<Button
					v-if="featureConfig.isPreview"
					icon="pi pi-times"
					class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
					@click="emit('close-preview')"
				/>
			</aside>
		</header>
		<template v-if="!hideIntro">
			<header id="asset-top" ref="headerRef">
				<section>
					<!-- put the buttons above the title if there is an overline -->
					<div v-if="overline" class="vertically-center">
						<span class="overline">{{ overline }}</span>
						<slot name="edit-buttons" />
					</div>
					<slot name="info-bar" />

					<!--For naming asset such as model or code file-->
					<div class="vertically-center">
						<slot name="name-input" />
						<h4 v-if="!isNamingAsset" v-html="name" class="nudge-down" />

						<div v-if="!overline" class="vertically-center">
							<slot name="edit-buttons" />
						</div>
					</div>

					<!--put model contributors here too-->
					<span class="authors" v-if="authors">
						<i :class="authors.includes(',') ? 'pi pi-users' : 'pi pi-user'" />
						<span v-html="authors" />
					</span>
					<div v-if="doi">
						DOI: <a :href="`https://doi.org/${doi}`" rel="noreferrer noopener" v-html="doi" />
					</div>
					<div v-if="publisher" v-html="publisher" />
					<!--created on: date-->
					<div class="header-buttons">
						<slot name="bottom-header-buttons" />
					</div>
				</section>
				<aside class="spread-out">
					<Button
						v-if="featureConfig.isPreview"
						icon="pi pi-times"
						class="close p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
						@click="emit('close-preview')"
					/>
				</aside>
			</header>
		</template>
		<section :style="stretchContentStyle">
			<slot name="default" />
		</section>
		<Teleport to="body">
			<tera-modal
				v-if="isCopyModalVisible"
				class="modal"
				@modal-mask-clicked="isCopyModalVisible = false"
			>
				<template #header>
					<h4>Make a copy</h4>
				</template>
				<template #default>
					<form>
						<label for="copy-asset">{{ copyNameInputPrompt }}</label>
						<InputText
							v-bind:class="invalidInputStyle"
							id="copy-asset"
							type="text"
							v-model="copiedAssetName"
							placeholder="Asset name"
						/>
					</form>
				</template>
				<template #footer>
					<Button @click="duplicateAsset">Copy asset</Button>
					<Button class="p-button-secondary" @click="isCopyModalVisible = false"> Cancel </Button>
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { ref, computed, watch, PropType } from 'vue';
import Button from 'primevue/button';
import { FeatureConfig } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import { logger } from '@/utils/logger';

const props = defineProps({
	name: {
		type: String,
		default: ''
	},
	overline: {
		type: String,
		default: null
	},
	authors: {
		type: String,
		default: null
	},
	doi: {
		type: String,
		default: null
	},
	publisher: {
		type: String,
		default: null
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	},
	// Duplication
	namesToNotDuplicate: {
		type: Array as PropType<String[]>,
		default: () => []
	},
	// Booleans default to false if not specified
	isNamingAsset: Boolean,
	hideIntro: Boolean,
	showStickyHeader: Boolean,
	stretchContent: Boolean
});

const emit = defineEmits(['close-preview', 'duplicate']);

const assetContainer = ref();
const headerRef = ref();
const scrollPosition = ref(0);

const copiedAssetName = ref<string>('');
const copyNameInputPrompt = ref('');
const isValidDuplciateName = ref<boolean>(true);
const isCopyModalVisible = ref(false);

const invalidInputStyle = computed(() => (!isValidDuplciateName.value ? 'p-invalid' : ''));

const shrinkHeader = computed(() => {
	const headerHeight = headerRef.value?.clientHeight ? headerRef.value.clientHeight - 50 : 1;
	return (
		scrollPosition.value > headerHeight && // Appear if (original header - 50px) is scrolled past
		scrollPosition.value !== 0 && // Handles case where original header is shorter than shrunk header (happens in PDF view)
		!props.isNamingAsset // Don't appear while creating an asset eg. a model
	);
});

// Scroll margin for anchors are adjusted depending on the header (inserted in css)
const scrollMarginTopStyle = computed(() => (shrinkHeader.value ? '3.5rem' : '0.5rem'));
const stretchContentStyle = computed(() =>
	props.stretchContent ? { gridColumn: '1 / span 2' } : {}
);

function updateScrollPosition(event) {
	scrollPosition.value = event?.currentTarget.scrollTop;
}

function getJustAssetName(modelName: string): string {
	let potentialNum: string = '';
	let completeParen: boolean = false;
	let idx = modelName.length;
	if (modelName.charAt(modelName.length - 1) === ')') {
		for (let i = modelName.length - 2; i >= 0; i--) {
			if (modelName.charAt(i) === '(') {
				completeParen = true;
				idx = i;
				break;
			}
			potentialNum = modelName.charAt(i) + potentialNum;
		}
	}

	if (completeParen && !Number.isNaN(potentialNum as any)) {
		return modelName.substring(0, idx).trim();
	}
	return modelName.trim();
}

function getSuggestedAssetName(currModelName: string, counter: number): string {
	const suggestedName = `${currModelName} (${counter})`;

	if (!props.namesToNotDuplicate.includes(suggestedName)) {
		return suggestedName;
	}
	return getSuggestedAssetName(currModelName, counter + 1);
}

function initiateAssetDuplication() {
	copyNameInputPrompt.value = 'What do you want to name it?';
	const assetName = getJustAssetName(props.name.trim());
	copiedAssetName.value = getSuggestedAssetName(assetName, 1);
	isCopyModalVisible.value = true;
}
defineExpose({ initiateAssetDuplication, isCopyModalVisible, assetContainer });

async function duplicateAsset() {
	if (props.namesToNotDuplicate.includes(copiedAssetName.value.trim())) {
		copyNameInputPrompt.value = 'Duplicate name - please enter a different name:';
		isValidDuplciateName.value = false;
		logger.info('Duplicate name - please enter a different name');
		return;
	}
	copyNameInputPrompt.value = 'Creating a copy...';
	isValidDuplciateName.value = true;
	emit('duplicate', copiedAssetName.value);
}

// Reset the scroll position to the top on asset change
watch(
	() => props.name,
	() => {
		document.getElementById('asset-top')?.scrollIntoView();
	}
);
</script>

<style scoped>
main {
	display: grid;
	/* minmax prevents grid blowout caused by datatable */
	grid-template-columns: auto minmax(0, 1fr);
	grid-template-rows: auto 1fr;
	height: 100%;
	background-color: var(--surface-section);
	/* accounts for sticky header height */
	scroll-margin-top: v-bind('scrollMarginTopStyle');
	overflow-y: auto;
	overflow-x: hidden;
}

main > section {
	grid-column-start: 2;
}

header {
	display: flex;
	flex-direction: row;
	height: fit-content;
	grid-column-start: 2;
	color: var(--text-color-subdued);
	padding: 0.5rem 1rem;
	transition: 0.2s;
	display: flex;
	gap: 1rem;
	align-items: center;
}

header.shrinked {
	height: 3rem;
	position: sticky;
	top: -1px;
	z-index: 100;
	isolation: isolate;
	background-color: rgba(255, 255, 255, 0.85);
	backdrop-filter: blur(6px);
	padding: 0.5rem 1rem;
	border-bottom: 1px solid var(--surface-border-light);
	box-shadow: 0px 4px 8px -7px #b8b8b8;
}

header.shrinked h4 {
	align-self: center;
	overflow: hidden;
	text-align: left;
	text-overflow: ellipsis;
	white-space: nowrap;
	max-width: var(--constrain-width);
	font-size: var(--font-body-small);
}

h4,
header section p {
	color: var(--text-color-primary);
}

header section,
header aside {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	max-width: var(--constrain-width);
}

header aside {
	align-self: flex-start;
	/* Prevent button stretch */
	max-height: 2.5rem;
}

header.shrinked aside {
	align-self: center;
}

.nudge-down {
	margin-top: 0.25rem;
}

.vertically-center {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 1rem;
}

main:deep(.p-inputtext.p-inputtext-sm) {
	padding: 0.65rem 0.65rem 0.65rem 3rem;
}

/* Input asset name */
header section:deep(> input) {
	width: var(--constrain-width);
	font-size: var(--font-body-medium);
}

.overline,
.authors {
	color: var(--text-color-primary);
}

.authors i {
	color: var(--text-color-primary);
	margin-right: 0.5rem;
}

.header-buttons,
header aside {
	display: flex;
	flex-direction: row;
	gap: 0.5rem;
}

/* Affects child components put in the slot*/
main:deep(.p-accordion) {
	margin: 0.5rem;
}

/*  Gives some top padding when you auto-scroll to an anchor */
main:deep(.p-accordion-header > a > header) {
	scroll-margin-top: v-bind('scrollMarginTopStyle');
}

main:deep(.p-accordion-content > p),
main:deep(.p-accordion-content > ul),
main:deep(.data-row) {
	max-width: var(--constrain-width);
}

main:deep(.p-accordion-content ul) {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	list-style: none;
}

main:deep(.p-accordion-content > textarea) {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 5px;
	resize: none;
	overflow-y: hidden;
	width: 100%;
}

main:deep(.artifact-amount) {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: 0.25rem;
}

/* These styles should probably be moved to the general theme in some form */
main:deep(input) {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	padding: 0.75rem;
}

main:deep(.p-button.p-button-outlined) {
	color: var(--text-color-primary);
	box-shadow: var(--text-color-disabled) inset 0 0 0 1px;
}

.spread-out {
	align-items: center;
	justify-content: space-between;
	flex-grow: 1;
}
</style>
