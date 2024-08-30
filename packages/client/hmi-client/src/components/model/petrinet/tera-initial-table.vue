<template>
	<Accordion multiple :active-index="[0]">
		<AccordionTab>
			<template #header>
				Initials <span class="artifact-amount">({{ numInitials }})</span>
				<tera-input-text v-model="filterText" placeholder="Filter" />
			</template>

			<ul class="pl-1">
				<li v-for="{ baseInitial, childInitials, isVirtual } in initialList" :key="baseInitial">
					<!-- Stratified -->
					<section v-if="isVirtual" class="initial-entry-stratified">
						<Accordion multiple>
							<AccordionTab>
								<template #header>
									<span>{{ baseInitial }}</span>
									<Button label="Open Matrix" text size="small" @click.stop="matrixModalId = baseInitial" />
								</template>
								<div class="flex">
									<ul class="ml-1">
										<li v-for="{ target } in childInitials" :key="target">
											<tera-initial-entry
												:model="model"
												:model-configuration="modelConfiguration"
												:modelConfigurations="modelConfigurations"
												:initial-id="target"
												@update-expression="emit('update-expression', $event)"
												@update-source="emit('update-source', $event)"
											/>
											<Divider type="solid" />
										</li>
									</ul>
								</div>
							</AccordionTab>
						</Accordion>
					</section>

					<!-- Unstratified -->
					<tera-initial-entry
						v-else
						:model="model"
						:model-configuration="modelConfiguration"
						:modelConfigurations="modelConfigurations"
						:initial-id="baseInitial"
						@update-expression="emit('update-expression', $event)"
						@update-source="emit('update-source', $event)"
					/>
					<Divider type="solid" />
				</li>
			</ul>
		</AccordionTab>
	</Accordion>

	<tera-stratified-matrix-modal
		v-if="matrixModalId && isStratified"
		:id="matrixModalId"
		:mmt="mmt"
		:mmt-params="mmtParams"
		:stratified-matrix-type="StratifiedMatrix.Initials"
		:open-value-config="!!matrixModalId"
		@close-modal="matrixModalId = ''"
		@update-cell-value="emit('update-expression', { id: $event.variableName, value: $event.newValue })"
	/>
</template>

<script setup lang="ts">
import { InitialSemantic, Model, ModelConfiguration } from '@/types/Types';
import { getInitials } from '@/services/model-configurations';
import { StratifiedMatrix } from '@/types/Model';
import { computed, ref } from 'vue';
import { collapseInitials, isStratifiedModel } from '@/model-representation/mira/mira';
import { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraStratifiedMatrixModal from './model-configurations/tera-stratified-matrix-modal.vue';
import TeraInitialEntry from './tera-initial-entry.vue';

const props = defineProps<{
	modelConfiguration: ModelConfiguration;
	modelConfigurations: ModelConfiguration[];
	model: Model;
	mmt: MiraModel;
	mmtParams: MiraTemplateParams;
}>();

const emit = defineEmits(['update-expression', 'update-source']);

const isStratified = isStratifiedModel(props.mmt);
const initialList = computed<
	{
		baseInitial: string;
		childInitials: InitialSemantic[];
		isVirtual: boolean;
	}[]
>(() => {
	const collapsedInitials = collapseInitials(props.mmt);
	const initials = getInitials(props.modelConfiguration);
	return Array.from(collapsedInitials.keys())
		.map((id) => {
			const childTargets = collapsedInitials.get(id) ?? [];
			const childInitials = childTargets
				.map((childTarget) => initials.find((i) => i.target === childTarget))
				.filter(Boolean) as InitialSemantic[];
			const isVirtual = childTargets.length > 1;
			const baseInitial = id;

			return { baseInitial, childInitials, isVirtual };
		})
		.filter(({ baseInitial }) => baseInitial.toLowerCase().includes(filterText.value.toLowerCase()));
});

const matrixModalId = ref('');

const numInitials = computed(() => initialList.value.length);

const filterText = ref('');
</script>

<style scoped>
ul {
	flex-grow: 1;
	li {
		list-style: none;
	}
}

.initial-entry-stratified {
	border-left: 4px solid var(--surface-300);
	padding-left: var(--gap-1);
}

.artifact-amount {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	margin-left: var(--gap-1);
	margin-right: auto;
}

:deep(.p-divider) {
	&.p-divider-horizontal {
		margin-top: var(--gap-2);
		margin-bottom: var(--gap-2);
		color: var(--gray-300);
	}
	&.p-divider-vertical {
		margin-left: var(--gap-small);
		margin-right: var(--gap);
	}
}
</style>
