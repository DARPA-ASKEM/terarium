<template>
	<transition>
		<div class="chart-settings-panel" v-if="activeSettings !== null">
			<header :class="{ shadow: false }">
				<Button :icon="`pi pi-times`" @click="$emit('close')" text rounded size="large" />
				<h4>Chart Settings</h4>
			</header>
			<div class="content">
				<div v-if="chartAnnotations !== undefined">
					<h5>Annotations</h5>
					{{ chartAnnotations }}
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
							:icon="'pi pi-sparkles'"
							:placeholder="'What do you want to annotate?'"
							:disabled="!!isGeneratingAnnotation"
							:model-value="generateAnnotationQuery"
							@keyup.enter="createAnnotation"
							@keyup.esc="cancelGenerateAnnotation"
						/>
					</div>
				</div>
			</div>
		</div>
	</transition>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import { ChartSetting } from '@/types/common';
import { ChartAnnotation } from '@/types/Types';
import TeraInputText from '@/components/widgets/tera-input-text.vue';

const props = defineProps<{
	activeSettings: ChartSetting | null;
	annotations?: ChartAnnotation[];
	generateAnnotation?: (setting: ChartSetting, query: string) => Promise<ChartAnnotation>;
}>();

const emit = defineEmits(['close', 'update:settings', 'delete-annotation', 'create-annotation']);

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
	generateAnnotationQuery.value = '';
	emit('create-annotation', newAnnotation);
};

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
	height: 100%;
	width: 100%;
	background: #fff;
	left: 0;

	&.v-enter-active,
	&.v-leave-active {
		transition: left 0.15s ease-in;
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
		padding-left: var(--gap);
		gap: var(--gap);
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
}
</style>
