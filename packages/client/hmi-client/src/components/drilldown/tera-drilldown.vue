<template>
	<aside class="overlay-container">
		<section :class="{ popover: props.popover }">
			<tera-drilldown-header
				:active-index="selectedViewIndex"
				:views="views"
				:tooltip="tooltip"
				:documentation-url="node.documentationUrl"
				@tab-change="handleTabChange"
				@close="emit('on-close-clicked')"
			>
				{{ title ?? node.displayName }}
				<template #top-header-actions>
					<aside class="input-chips mr-auto">
						<Chip
							v-for="(input, index) in node.inputs.filter((input) => input.value)"
							:key="index"
							:label="input.label"
						>
							<template #icon>
								<tera-operator-port-icon v-if="input.type" :portType="input.type" />
							</template>
						</Chip>
					</aside>
					<template v-if="outputOptions && selectedOutputId">
						<tera-output-dropdown
							class="mx-2"
							:options="outputOptions"
							:output="selectedOutputId"
							@update:selection="(e) => emit('update:selection', e)"
						/>
						<section v-if="!isEmpty(menuItems)" class="mx-2">
							<Button icon="pi pi-ellipsis-v" rounded text @click.stop="toggle" />
							<Menu ref="menu" :model="menuItems" :popup="true" />
						</section>
					</template>
				</template>
				<template #actions>
					<slot name="header-actions" />
					<tera-operator-annotation
						:state="node.state"
						@update-state="(state: any) => emit('update-state', state)"
					/>
				</template>
			</tera-drilldown-header>
			<main class="flex overflow-hidden h-full">
				<slot name="sidebar" />
				<tera-columnar-panel class="flex-1">
					<template v-for="(tab, index) in tabs" :key="index">
						<!--
							TODO: We used to use v-show here but it ruined the rendering of tera-model-diagram
							if it was in the unselected tab. For now we are using v-if but we may want to
							use css to hide the unselected tab content instead.
						-->
						<component :is="tab" v-if="selectedViewIndex === index" />
					</template>
					<section v-if="slots.preview">
						<slot name="preview" />
					</section>
				</tera-columnar-panel>
			</main>
			<footer v-if="slots.footer">
				<slot name="footer" />
			</footer>
		</section>
	</aside>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref, useSlots } from 'vue';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import Menu from 'primevue/menu';
import { TabViewChangeEvent } from 'primevue/tabview';
import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';
import TeraDrilldownHeader from '@/components/drilldown/tera-drilldown-header.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import TeraOperatorAnnotation from '@/components/operator/tera-operator-annotation.vue';
import TeraOperatorPortIcon from '@/components/operator/tera-operator-port-icon.vue';
import TeraOutputDropdown from '@/components/drilldown/tera-output-dropdown.vue';

const props = defineProps<{
	node: WorkflowNode<any>;
	menuItems?: any[];
	title?: string;
	tooltip?: string;
	popover?: boolean;
}>();

const emit = defineEmits([
	'on-close-clicked',
	'update-state',
	'update:selection',
	'update-output-port'
]);
const slots = useSlots();
const menu = ref();
/**
 * This will retrieve and filter all top level components in the default slot if they have the tabName prop.
 */
const tabs = computed(() => {
	if (slots.default?.()) {
		if (slots.default().length === 1) {
			// if there is only 1 component we don't need to know the tab name and we can render it.
			return slots.default();
		}

		return slots.default().filter((vnode) => vnode.props?.tabName);
	}
	return [];
});

const views = computed(() => tabs.value.map((vnode) => vnode.props?.tabName));

const selectedViewIndex = ref<number>(0);
const handleTabChange = (event: TabViewChangeEvent) => {
	selectedViewIndex.value = event.index;
};

const selectedTab = computed(() => views.value[selectedViewIndex.value]);
defineExpose({ selectedTab });

const selectedOutputId = computed(() => {
	if (props.node.active) {
		return props.node.active;
	}
	return null;
});
const outputOptions = computed(() => {
	// We do not display output selection for Asset operators
	if (
		[
			WorkflowOperationTypes.MODEL,
			WorkflowOperationTypes.DATASET,
			WorkflowOperationTypes.DOCUMENT,
			WorkflowOperationTypes.CODE
		].includes(props.node.operationType)
	) {
		return null;
	}

	if (isEmpty(props.node.outputs)) return [];

	return [
		{
			label: 'Select outputs to display in operator',
			items: props.node.outputs
		}
	];
});

const toggle = (event) => {
	menu.value.toggle(event);
};
</script>

<style scoped>
.overlay-container {
	isolation: isolate;
	z-index: var(--z-index, var(--z-index-modal));
	position: fixed;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.32);
}

/* There is a performance issue with these large modals.
When scrolling it takes time to render the content, particularly heavy content such as the LLM integrations. This will show
us the main application behind the modal temporarily as content loads when scrolling which is a bit of an eye sore.
An extra div here is used to alleviate the impact of these issues a little by allowing us to see the overlay container rather
than the main application behind the modal when these render issues come, however this is still an issue regardless.
*/
.overlay-container > section {
	height: calc(100% - 1rem);
	margin: 1rem 1.5rem 0rem 1.5rem;
	background: var(--surface-0);
	border-radius: var(--modal-border-radius) var(--modal-border-radius) 0 0;
	display: flex;
	flex-direction: column;
	overflow: hidden;
	&.popover {
		margin: 3rem 2.5rem 0rem 2.5rem;
	}
}

footer {
	padding: 0 1.5rem 1rem 1.5rem;
	display: flex;
	justify-content: flex-end;
	gap: 0.5rem;
}

.input-chips {
	display: flex;
	gap: var(--gap-1-5);
	margin: 0 var(--gap);
}

.p-chip {
	background-color: var(--surface-section);
	color: var(--text-color-primary);
}

:deep(.p-chip .p-chip-text) {
	font-size: var(--font-caption);
}
</style>
