<template>
	<!-- Stratified -->
	<Accordion v-if="isStratified" multiple>
		<AccordionTab
			v-for="[key, values] in collapseParameters(props.mmt, props.mmtParams).entries()"
			:key="key"
		>
			<template #header>
				<span>{{ key }}</span>
				<Button label="Open Matrix" text size="small" @click.stop="matrixModalId = key" />
			</template>
			<div class="flex">
				<Divider layout="vertical" type="solid" />
				<ul>
					<li v-for="parameter in values" :key="parameter">
						<tera-parameter-entry
							:model-configuration="props.modelConfiguration"
							:parameter-id="parameter"
							@update-constant="emit('update-constant', $event)"
							@update-distribution="emit('update-distribution', $event)"
							@update-source="emit('update-source', $event)"
							@select-distribution="emit('select-distribution', $event)"
						/>
						<Divider type="solid" />
					</li>
				</ul>
			</div>
		</AccordionTab>
	</Accordion>

	<!-- Unstratified -->
	<ul v-else class="flex-grow">
		<li v-for="{ id } in getParameters(modelConfiguration)" :key="id">
			<tera-parameter-entry
				:model-configuration="modelConfiguration"
				:parameter-id="id"
				@update-constant="emit('update-constant', $event)"
				@update-distribution="emit('update-distribution', $event)"
				@update-source="emit('update-source', $event)"
				@select-distribution="emit('select-distribution', $event)"
			/>
			<Divider type="solid" />
		</li>
	</ul>

	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalId && isStratified"
			:id="matrixModalId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			:open-value-config="!!matrixModalId"
			@close-modal="matrixModalId = ''"
			@update-cell-value="
				emit('update-constant', { id: $event.variableName, value: $event.newValue })
			"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { getParameters } from '@/services/model-configurations';
import { StratifiedMatrix } from '@/types/Model';
import { ref } from 'vue';
import { collapseParameters, isStratifiedModel } from '@/model-representation/mira/mira';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';
import TeraParameterEntry from './tera-parameter-entry.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits([
	'update-constant',
	'update-distribution',
	'update-source',
	'delete-distribution',
	'select-distribution'
]);

const isStratified = isStratifiedModel(props.mmt);

const matrixModalId = ref('');
</script>

<style scoped>
ul {
	flex-grow: 1;
	li {
		list-style: none;
	}
}

:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: 0;
		margin-bottom: var(--gap);
		color: var(--gray-300);
	}
	&.p-divider-vertical {
		margin-left: var(--gap-small);
		margin-right: var(--gap);
	}
}
</style>
