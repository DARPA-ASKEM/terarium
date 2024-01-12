<template>
	<div class="value-editor">
		<!-- FIXME: may want to replace section with something that handles long initials list better -->
		<section>
			<div>
				<h4>Initials</h4>
				<Accordion v-if="stratifiedModelType && !_.isEmpty(initials)" :multiple="true">
					<AccordionTab v-for="[init, vals] in initials" :key="init">
						<template #header>
							{{ init }}
						</template>
						<ul>
							<li v-for="val in vals" :key="val">
								<Button
									:label="val"
									class="asset-button"
									plain
									text
									size="small"
									@click="getClickedField('initial', val)"
								/>
							</li>
						</ul>
					</AccordionTab>
				</Accordion>
				<ul v-else-if="!stratifiedModelType && !_.isEmpty(initials)">
					<li v-for="[init, vals] in initials" :key="init">
						<Button
							:label="init"
							class="asset-button"
							plain
							text
							size="large"
							@click="getClickedField('initial', vals[0])"
						/>
					</li>
				</ul>
			</div>
			<div>
				<h4>Parameters</h4>
				<Accordion v-if="stratifiedModelType && !_.isEmpty(parameters)" :multiple="true">
					<AccordionTab v-for="[param, vals] in parameters" :key="param">
						<template #header>
							{{ param }}
						</template>
						<ul>
							<li v-for="val in vals" :key="val">
								<Button
									:label="val"
									class="asset-button"
									plain
									text
									size="small"
									@click="getClickedField('parameter', val)"
								/>
							</li>
						</ul>
					</AccordionTab>
				</Accordion>
				<ul v-else-if="!stratifiedModelType && !_.isEmpty(parameters)">
					<li v-for="[param, vals] in parameters" :key="param">
						<Button
							:label="param"
							class="asset-button"
							plain
							text
							size="large"
							@click="getClickedField('parameter', vals[0])"
						/>
					</li>
				</ul>
			</div>
		</section>
		<!-- FIXME: may want to replace section with something that handles long parameters list better -->
		<section>
			<div class="content-container">
				<div v-if="fieldType === 'parameter'" class="form-section">
					<h4>
						{{
							currentParam?.name ? `${currentParam.name} | ${currentParam.id}` : currentParam?.id
						}}
					</h4>
					<div v-if="currentParam?.description">
						<h3>Description</h3>
						<p>{{ currentParam?.description }}</p>
					</div>
					<div>
						<h3>Value</h3>
						<InputNumber
							class="p-inputtext-sm"
							inputId="numericInput"
							mode="decimal"
							:min-fraction-digits="1"
							v-model="paramValue"
							@update:model-value="updateParameter(paramValue)"
						/>
					</div>
				</div>
				<div v-else-if="fieldType === 'initial'" class="form-section">
					<h4>{{ currentInitial?.target }}</h4>
					<div>
						<h3>Expression</h3>
						<InputText
							class="p-inputtext-sm"
							v-model="initialExpression"
							@update:model-value="updateInitial(initialExpression)"
						/>
					</div>
				</div>
				<h4 v-else>Select a field to edit</h4>
			</div>
		</section>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import { Model, Initial, ModelParameter } from '@/types/Types';
import {
	getUnstratifiedParameters,
	getUnstratifiedInitials
} from '@/model-representation/petrinet/mira-petri';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';

const props = defineProps<{
	model: Model;
	initials: Initial[];
	parameters: ModelParameter[];
}>();

const emit = defineEmits(['update-param', 'update-initial']);

const stratifiedModelType = computed(() => getStratificationType(props.model));

const parameters = computed<Map<string, string[]>>(() => getUnstratifiedParameters(props.model));
const currentParam = ref<ModelParameter>();
const paramValue = ref<number>();

const initials = computed<Map<string, string[]>>(() => getUnstratifiedInitials(props.model));
const currentInitial = ref<Initial>();
const initialExpression = ref<string>();

const fieldType = ref<string>();
const selectedField = ref<string>();

const getClickedField = (type: string, field: string) => {
	fieldType.value = type;
	selectedField.value = field;

	if (type === 'parameter') {
		currentParam.value = props.parameters.find((p) => p.id === field);
		paramValue.value = currentParam.value?.value;
	} else if (type === 'initial') {
		currentInitial.value = props.initials.find((i) => i.target === field);
		initialExpression.value = currentInitial.value?.expression;
	}
};

const updateParameter = (paramVal?: number) => {
	if (paramVal) {
		emit('update-param', { ...currentParam.value, value: paramVal });
	}
};

const updateInitial = (initialExp?: string) => {
	if (initialExp) {
		emit('update-initial', { ...currentInitial.value, expression: initialExp });
	}
};

// When output changes via the dropdown, force the editor to update
watch(
	[() => props.initials, () => props.parameters],
	() => {
		if (fieldType.value && selectedField.value) {
			getClickedField(fieldType.value, selectedField.value);
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
ul {
	list-style: none;
}

.value-editor {
	display: flex;
	flex-direction: row;

	& > *:first-child {
		flex: 1;
	}

	& > *:last-child {
		flex: 3;
	}
}

.content-container {
	display: flex;
	flex-direction: column;
	background-color: var(--surface-50);
	flex-grow: 1;
	padding: var(--gap);
	border-radius: var(--border-radius-medium);
	box-shadow: 0px 0px 4px 0px rgba(0, 0, 0, 0.25) inset;
	overflow: hidden;
	height: 100%;
}

.form-section {
	display: flex;
	flex-direction: column;
	gap: var(--gap);
}
</style>
