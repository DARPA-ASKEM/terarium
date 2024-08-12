<template>
	<div class="vega-chart-container">
		<p v-if="renderErrorMessage" class="p-error">
			{{ renderErrorMessage }}
		</p>
		<div ref="vegaContainer"></div>
		<footer>
			<slot name="footer" />
		</footer>
	</div>
</template>

<script setup lang="ts">
import embed, { Result, VisualizationSpec } from 'vega-embed';
import { Config as VgConfig } from 'vega';
import { Config as VlConfig } from 'vega-lite';

import { ref, watch, toRaw, isRef, isReactive, isProxy } from 'vue';

export type Config = VgConfig | VlConfig;

const props = withDefaults(
	defineProps<{
		visualizationSpec: VisualizationSpec;
		areEmbedActionsVisible?: boolean;
		/**
		 * A list of signal names for Vega interval-type selections.
		 * This is equivalent to the name of the `param` in the Vega Lite spec.
		 * The chart will listen to signals for that selection, and emit the selected interval, or null
		 * if the selection is empty.
		 * See https://vega.github.io/vega/docs/api/view/#view_signal for more information.
		 */
		intervalSelectionSignalNames?: string[];
		config?: Config | null;
	}>(),
	{
		areEmbedActionsVisible: true,
		intervalSelectionSignalNames: () => [],
		config: null
	}
);
const vegaContainer = ref<HTMLElement>();
const vegaVisualization = ref<Result>();
const renderErrorMessage = ref<String>();

const emit = defineEmits<{
	(
		e: 'update-interval-selection',
		signalName: string,
		intervalExtent: { [fieldName: string]: [number, number] } | null
	): void;
	(e: 'chart-click', datum: any | null): void;
}>();

/**
 * deepToRaw() is a recursive 'deep' version of Vue's `toRaw` function, which converts
 * Vue created Refs/Proxy objects back to their original form.
 *
 * In certain cases, such as when a nested object (like a Vega Spec) is wrapped in a
 * Vue ref (including when it is returned using `defineModel` on a component like
 * `encoding` is in the `ChartChannelEncodingConfigurator`), each nested object is
 * wrapped in its own `Proxy`. Certain libraries don't deal well with `Proxy` objects
 * and they need to be converted back to raw objects. Vue's `toRaw` function only
 * handles the first level of Proxy objects. This function will recursively go through
 * an object with nested Proxy objects and unwrap them all.
 *
 * This function is taken from https://github.com/vuejs/core/issues/5303
 */
function deepToRaw<T extends Record<string, any>>(sourceObj: T): T {
	const objectIterator = (input: any): any => {
		if (Array.isArray(input)) {
			return input.map((item) => objectIterator(item));
		}
		if (isRef(input) || isReactive(input) || isProxy(input)) {
			return objectIterator(toRaw(input));
		}
		if (input && typeof input === 'object') {
			return Object.keys(input).reduce((acc, key) => {
				acc[key as keyof typeof acc] = objectIterator(input[key]);
				return acc;
			}, {} as T);
		}
		return input;
	};
	return objectIterator(sourceObj);
}

async function updateVegaVisualization(container: HTMLElement, visualizationSpec: VisualizationSpec) {
	renderErrorMessage.value = undefined;
	vegaVisualization.value = undefined;
	try {
		vegaVisualization.value = await embed(
			container,
			{ ...visualizationSpec },
			{
				config: props.config || {},
				actions: props.areEmbedActionsVisible === false ? false : undefined
			}
		);
		const { view } = vegaVisualization.value;
		props.intervalSelectionSignalNames.forEach((signalName) => {
			view.addSignalListener(signalName, (name, valueRange: { [fieldName: string]: [number, number] }) => {
				if (valueRange === undefined || Object.keys(valueRange).length === 0) {
					emit('update-interval-selection', name, null);
					return;
				}
				emit('update-interval-selection', name, valueRange);
			});
		});
		view.addEventListener('click', (_event, item) => {
			emit('chart-click', item?.datum ?? null);
		});
	} catch (e) {
		// renderErrorMessage.value = getErrorMessage(e);
		// renderErrorMessage.value = e;
	}
}

watch([vegaContainer, () => props.visualizationSpec], () => {
	if (!vegaContainer.value) {
		return;
	}
	const spec = deepToRaw(props.visualizationSpec);
	updateVegaVisualization(vegaContainer.value, spec);
});
</script>
<style scoped>
.vega-chart-container {
	background: var(--surface-a);
	border-radius: var(--border-radius-medium);
	border: 1px solid var(--surface-border-light);
	margin-bottom: var(--gap-4);
	padding-top: var(--gap-2);
	footer {
		padding: var(--gap-3);
	}
}

/* adjust style, position and rotation of action button */
:deep(.vega-embed.has-actions) {
	padding-right: 0px;
	position: relative;
}
:deep(.vega-embed summary) {
	border: 1px solid transparent;
	opacity: 1;
	box-shadow: none;
}
:deep(.vega-embed summary):hover {
	border: 1px solid transparent;
	background: var(--surface-hover);
}
:deep(.vega-embed summary svg) {
	transform: rotate(90deg);
}
:deep(.vega-embed .vega-actions a) {
	font-family: 'Figtree', sans-serif;
	font-weight: 400;
}
</style>
