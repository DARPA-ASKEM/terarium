<template>
	<!-- Stratified -->
	<template v-if="isStratified">
		<Accordion multiple>
			<AccordionTab v-for="[key, values] in collapseInitials(props.mmt).entries()" :key="key">
				<template #header>
					<span>{{ key }}</span>
					<Button label="Open Matrix" text size="small" @click.stop="openMatrix(key)" />
				</template>
				<div class="flex">
					<Divider layout="vertical" type="solid" />
					<ul>
						<li v-for="initial in values" :key="initial">
							<tera-initial-entry
								:model-configuration="props.modelConfiguration"
								:initial-id="initial"
								@update-expression="emit('update-expression', $event)"
								@update-source="emit('update-source', $event)"
							/>
							<Divider type="solid" />
						</li>
					</ul>
				</div>
			</AccordionTab>
		</Accordion>
	</template>

	<!-- Unstratified -->
	<template v-else>
		<ul class="flex-grow">
			<li v-for="{ target } in getInitials(modelConfiguration)" :key="target">
				<tera-initial-entry
					:model-configuration="modelConfiguration"
					:initial-id="target"
					@update-expression="emit('update-expression', $event)"
					@update-source="emit('update-source', $event)"
				/>
				<Divider type="solid" />
			</li>
		</ul>
	</template>

	<Teleport to="body">
		<tera-stratified-matrix-modal
			v-if="matrixModalContext.isOpen && isStratified"
			:id="matrixModalContext.matrixId"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:stratified-matrix-type="StratifiedMatrix.Initials"
			:open-value-config="matrixModalContext.isOpen"
			@close-modal="matrixModalContext.isOpen = false"
			@update-cell-value="
				emit('update-expression', { id: $event.variableName, value: $event.newValue })
			"
		/>
	</Teleport>
</template>

<script setup lang="ts">
import { ModelConfiguration } from '@/types/Types';
import { getInitials } from '@/services/model-configurations';
import { StratifiedMatrix } from '@/types/Model';
import { computed, ref } from 'vue';
import { collapseInitials, isStratifiedModel } from '@/model-representation/mira/mira';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';
import TeraInitialEntry from './tera-initial-entry.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-expression', 'update-source']);

const isStratified = computed(() => isStratifiedModel(props.mmt));

const matrixModalContext = ref({
	isOpen: false,
	matrixId: ''
});

function openMatrix(matrixId: string) {
	matrixModalContext.value.isOpen = true;
	matrixModalContext.value.matrixId = matrixId;
}
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
