<template>
	<transition>
		<div class="chart-settings-panel" v-if="activeSettings !== null">
			<header :class="{ shadow: false }">
				<Button :icon="`pi pi-angle-double-right`" @click="$emit('close')" text rounded size="large" />
				<h4>{{ activeSettings.name }}</h4>
			</header>
			<div class="content">
				<div class="annotation-items">
					<h5>Options</h5>
					<tera-checkbox
						label="Use log scale"
						:model-value="Boolean(useLog)"
						@update:model-value="toggleLogScale($event)"
					/>
				</div>

				<div v-if="chartAnnotations !== undefined" class="annotation-items">
					<h5>Annotations</h5>
					<div v-for="annotation in chartAnnotations" :key="annotation.id" class="annotation-item">
						{{ annotation.description }}
						<span class="btn-wrapper">
							<Button icon="pi pi-trash" rounded text @click="$emit('delete-annotation', annotation.id)" />
						</span>
					</div>
					<div>
						<Button
							v-if="!showAnnotationInput"
							class="p-button-sm p-button-text"
							icon="pi pi-plus"
							label="Add annotation"
							@click="showAnnotationInput = true"
						/>
						<tera-input-text
							v-if="showAnnotationInput"
							v-model="generateAnnotationQuery"
							:icon="'pi pi-sparkles'"
							:placeholder="'What do you want to annotate?'"
							:disabled="!!isGeneratingAnnotation"
							@keyup.enter="createAnnotationDebounced"
							@keyup.esc="cancelGenerateAnnotation"
							class="annotation-input"
						/>
					</div>
				</div>
				<section v-if="isColorPickerEnabled">
					<h6>Color Picker</h6>
					<input type="color" :value="activeSettings?.primaryColor ?? ''" @change="onColorChange($event)" />
				</section>
			</div>
		</div>
	</transition>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { ChartAnnotation } from '@/types/Types';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';

const props = defineProps<{
	activeSettings: ChartSetting | null;
	annotations?: ChartAnnotation[];
	/**
	 * We receives generateAnnotation as a functor from the parent to access the parent scope directly. This allows us to utilize dependencies defined in the parent component without passing them all as props, which can be cumbersome.
	 * Additionally, it enables us to handle post-generation actions (like resetting loading state or clearing input) after function completion.
	 * @param setting ChartSetting
	 * @param query llm query to generate annotation
	 */
	generateAnnotation?: (setting: ChartSetting, query: string) => Promise<ChartAnnotation | null>;
}>();

const emit = defineEmits(['close', 'delete-annotation', 'create-annotation', 'update-settings']);

// Log scale
const useLog = computed(() => props.activeSettings?.scale === 'log');
const toggleLogScale = (useLogScale: boolean) => {
	emit('update-settings', { scale: useLogScale ? 'log' : '' });
};

// Primary color
const isColorPickerEnabled = computed(() => {
	const type = props.activeSettings?.type;
	if (type) {
		return ![ChartSettingType.ERROR_DISTRIBUTION, ChartSettingType.VARIABLE_COMPARISON].includes(type);
	}
	return false;
});
const onColorChange = (event) => {
	emit('update-settings', { primaryColor: event.target?.value });
};

// Chart Annotations
const chartAnnotations = computed(() => {
	if (props.annotations === undefined) {
		return undefined;
	}
	return props.annotations.filter((annotation) => annotation.chartId === props.activeSettings?.id);
});
const isGeneratingAnnotation = ref(false);
const generateAnnotationQuery = ref<string>('');
const showAnnotationInput = ref<Boolean>(false);

const createAnnotation = async () => {
	if (props.generateAnnotation === undefined || props.activeSettings === null) {
		return;
	}
	isGeneratingAnnotation.value = true;
	const newAnnotation = await props.generateAnnotation(props.activeSettings, generateAnnotationQuery.value);
	isGeneratingAnnotation.value = false;
	showAnnotationInput.value = false;
	generateAnnotationQuery.value = '';
	emit('create-annotation', newAnnotation);
};
// Note: For some reason, @keyup.enter event on <tera-input-text> is fired twice. Let's introduce a debounced function to make sure the function is called only once.
const createAnnotationDebounced = _.debounce(createAnnotation, 100);

const cancelGenerateAnnotation = () => {
	generateAnnotationQuery.value = '';
	showAnnotationInput.value = false;
};
</script>

<style scoped>
.chart-settings-panel {
	position: absolute;
	top: 0;
	z-index: 3;
	margin-top: 50px;
	height: calc(100% - 50px);
	width: 100%;
	background: #fff;
	left: 2px;
	border: solid 1px var(--surface-border-light);

	&.v-enter-active,
	&.v-leave-active {
		transition: left 0.15s ease-out;
		left: 30%;
	}
	&.v-enter-from,
	&.v-leave-to {
		left: 100%;
	}

	header {
		position: sticky;
		top: 0;
		z-index: 3;
		display: flex;
		align-items: center;
		flex-direction: row-reverse;
		justify-content: space-between;
		padding: var(--gap-2);
		padding-left: var(--gap-4);
		gap: var(--gap-4);
		background-color: rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(3px);
		&.shadow {
			box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.1);
		}
		button {
			height: 2.5rem;
		}
	}
	.content {
		padding: var(--gap-4);
	}

	.annotation-input:deep(main) {
		padding: var(--gap-2-5) var(--gap-2);
	}
	.annotation-items {
		display: flex;
		padding-bottom: var(--gap-4);
		flex-direction: column;
		gap: var(--gap-2);

		.annotation-item {
			position: relative;
			padding: var(--gap-3);
			padding-left: var(--gap-4);
			padding-right: var(--gap-9);
			background: var(--surface-50);
			border-left: 4px solid var(--gray-600);
		}
		.btn-wrapper {
			position: absolute;
			top: 0;
			right: var(--gap-2);
			display: flex;
			flex-direction: column;
			justify-content: center;
			height: 100%;
		}
	}
}
</style>
