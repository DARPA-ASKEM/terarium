<template>
	<div class="value-editor">
		<!-- FIXME: may want to replace section with something that handles long initials list better -->
		<section>
			<div>
				<h4>Initials</h4>
				<!-- TODO: create a separate component for this custom "Accordion" -->
				<div v-if="stratifiedModelType && !_.isEmpty(initials)">
					<div v-for="([init, vals], idx) in initials" :key="init">
						<div>
							<Button
								:icon="openInitials.includes(idx) ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
								class="p-button-sm p-button-text"
								@click="toggleAccordion(FieldTypes.StratifiedInitial, idx)"
							/>
							<Button
								:label="init"
								class="p-button-text"
								@click="getClickedField(FieldTypes.StratifiedInitial, init)"
							/>
						</div>
						<ul v-if="openInitials.includes(idx)">
							<li v-for="val in vals" :key="val">
								<Button
									:label="val"
									class="asset-button"
									plain
									text
									size="small"
									@click="getClickedField(FieldTypes.Initial, val)"
								/>
							</li>
						</ul>
					</div>
				</div>
				<ul v-else-if="!stratifiedModelType && !_.isEmpty(initials)">
					<li v-for="[init, vals] in initials" :key="init">
						<Button
							:label="init"
							class="asset-button"
							plain
							text
							size="large"
							@click="getClickedField(FieldTypes.Initial, vals[0])"
						/>
					</li>
				</ul>
			</div>
			<div>
				<h4>Parameters</h4>
				<!-- TODO: create a separate component for this custom "Accordion" -->
				<div v-if="stratifiedModelType && !_.isEmpty(parameters)">
					<div v-for="([param, vals], idx) in parameters" :key="param">
						<div>
							<Button
								:icon="openParameters.includes(idx) ? 'pi pi-chevron-down' : 'pi pi-chevron-right'"
								class="p-button-sm p-button-text"
								@click="toggleAccordion(FieldTypes.StratifiedParameter, idx)"
							/>
							<Button
								:label="param"
								class="p-button-text"
								@click="getClickedField(FieldTypes.StratifiedParameter, param)"
							/>
						</div>
						<ul v-if="openParameters.includes(idx)">
							<li v-for="val in vals" :key="val">
								<Button
									:label="val"
									class="asset-button"
									plain
									text
									size="small"
									@click="getClickedField(FieldTypes.Parameter, val)"
								/>
							</li>
						</ul>
					</div>
				</div>
				<ul v-else-if="!stratifiedModelType && !_.isEmpty(parameters)">
					<li v-for="[param, vals] in parameters" :key="param">
						<Button
							:label="param"
							class="asset-button"
							plain
							text
							size="large"
							@click="getClickedField(FieldTypes.Parameter, vals[0])"
						/>
					</li>
				</ul>
			</div>
		</section>
		<!-- FIXME: may want to replace section with something that handles long parameters list better -->
		<section>
			<div class="content-container">
				<div v-if="fieldType === FieldTypes.StratifiedParameter" class="form-section">
					<h4>{{ paramBase }}</h4>
					<tera-stratified-matrix
						:model-configuration="basicModelConfig"
						:id="paramBase!"
						:stratified-model-type="stratifiedModelType!"
						:stratified-matrix-type="StratifiedMatrix.Parameters"
						:should-eval="false"
						@update-configuration="updateParametersFromMatrix"
					/>
				</div>
				<div v-else-if="fieldType === FieldTypes.StratifiedInitial" class="form-section">
					<h4>{{ initBase }}</h4>
					<tera-stratified-matrix
						:model-configuration="basicModelConfig"
						:id="initBase!"
						:stratified-model-type="stratifiedModelType!"
						:stratified-matrix-type="StratifiedMatrix.Initials"
						:should-eval="false"
						@update-configuration="updateInitialsFromMatrix"
					/>
				</div>
				<div v-else-if="fieldType === FieldTypes.Parameter" class="form-section">
					<h4>
						{{
							currentParam?.name ? `${currentParam.name} | ${currentParam.id}` : currentParam?.id
						}}
					</h4>
					<div v-if="currentParam?.description">
						<h3>Description</h3>
						<p>{{ currentParam?.description }}</p>
					</div>
					<div v-if="currentParam?.value">
						<h3>Value</h3>
						<InputNumber
							class="p-inputtext-sm"
							inputId="numericInput"
							mode="decimal"
							:min-fraction-digits="1"
							:max-fraction-digits="10"
							v-model="currentParam.value"
							@update:model-value="updateParameter"
						/>
					</div>
					<div v-if="currentParam?.distribution">
						<!-- TODO: add dropdown for more distribution types, right now there's
                            only support for uniform distribution so we're just hardcoding it
                        -->
						<div class="distribution-section">
							<h3>Distribution: {{ currentParam.distribution.type }}</h3>
							<div>
								<i
									class="trash-button pi pi-trash"
									:style="'cursor: pointer'"
									@click="removeDistribution"
								/>
							</div>
						</div>
						<div class="distribution-section">
							<label>Min</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="numericInput"
								mode="decimal"
								:min-fraction-digits="1"
								:max-fraction-digits="10"
								v-model="currentParam.distribution.parameters.minimum"
								@update:model-value="updateParameter"
							/>
							<label>Max</label>
							<InputNumber
								class="p-inputtext-sm"
								inputId="numericInput"
								mode="decimal"
								:min-fraction-digits="1"
								:max-fraction-digits="10"
								v-model="currentParam.distribution.parameters.maximum"
								@update:model-value="updateParameter"
							/>
						</div>
					</div>
					<div v-else-if="!currentParam?.distribution">
						<h3>Distribution</h3>
						<Button
							icon="pi pi-plus"
							class="p-button-sm p-button-text"
							label="Add Distribution"
							@click="addDistribution"
						/>
					</div>
				</div>
				<div v-else-if="fieldType === FieldTypes.Initial" class="form-section">
					<h4>{{ currentInitial?.target }}</h4>
					<div v-if="currentInitial?.expression">
						<h3>Expression</h3>
						<InputText
							class="p-inputtext-sm"
							v-model="currentInitial.expression"
							@update:model-value="updateInitial"
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
import { computed, ref, Ref, watch } from 'vue';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import { Model, ModelConfiguration, ModelParameter } from '@/types/Types';
import {
	getUnstratifiedParameters,
	getUnstratifiedInitials
} from '@/model-representation/petrinet/mira-petri';
import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
import TeraStratifiedMatrix from '@/components/model/petrinet/model-configurations/tera-stratified-matrix.vue';
import { StratifiedMatrix } from '@/types/Model';

enum FieldTypes {
	Parameter = 'parameter',
	Initial = 'initial',
	StratifiedParameter = 'stratified-parameter',
	StratifiedInitial = 'stratified-initial'
}

const props = defineProps<{
	model: Model;
	initials: Initial[];
	parameters: ModelParameter[];
}>();

const emit = defineEmits(['update-param', 'update-initial']);

const openInitials = ref<number[]>([]);
const openParameters = ref<number[]>([]);

const basicModelConfig = ref<ModelConfiguration>({
	id: '',
	name: '',
	modelId: props.model.id,
	configuration: props.model
});

const stratifiedModelType = computed(() => getStratificationType(props.model));

const parameters = computed<Map<string, string[]>>(() => getUnstratifiedParameters(props.model));
const currentParam = ref<ModelParameter>();
const paramBase = ref<string>();

const initials = computed<Map<string, string[]>>(() => getUnstratifiedInitials(props.model));
const currentInitial = ref<Initial>();
const initBase = ref<string>();

const fieldType = ref<FieldTypes>();
const selectedField = ref<string>();

const toggleAccordion = (type: FieldTypes, index: number) => {
	if (type === FieldTypes.StratifiedInitial) {
		toggleAccordionHelper(openInitials, index);
	} else if (type === FieldTypes.StratifiedParameter) {
		toggleAccordionHelper(openParameters, index);
	}
};

const toggleAccordionHelper = (openIndices: Ref<number[]>, index: number) => {
	if (openIndices.value.includes(index)) {
		openIndices.value = openIndices.value.filter((i) => i !== index);
	} else {
		openIndices.value = [...openIndices.value, index];
	}
};

const getClickedField = (type: FieldTypes, field: string) => {
	fieldType.value = type;
	selectedField.value = field;

	if (type === FieldTypes.StratifiedParameter) {
		paramBase.value = field;
	} else if (type === FieldTypes.StratifiedInitial) {
		initBase.value = field;
	} else if (type === FieldTypes.Parameter) {
		currentParam.value = props.parameters.find((p) => p.id === field);
	} else if (type === FieldTypes.Initial) {
		currentInitial.value = props.initials.find((i) => i.target === field);
	}
};

const addDistribution = () => {
	if (currentParam.value) {
		currentParam.value.distribution = {
			type: 'Uniform1',
			parameters: {
				minimum: 0,
				maximum: 0
			}
		};
	}
};

const removeDistribution = () => {
	if (currentParam.value) {
		currentParam.value.distribution = undefined;
	}
	updateParameter();
};

const updateParameter = () => {
	if (currentParam.value) {
		emit('update-param', [currentParam.value]);
	}
};

const updateInitial = () => {
	if (currentInitial.value) {
		emit('update-initial', [currentInitial.value]);
	}
};

const updateParametersFromMatrix = (configToUpdate: ModelConfiguration) => {
	const newParams = configToUpdate.configuration?.semantics.ode.parameters;
	if (newParams) {
		emit('update-param', newParams, true);
	}
};

const updateInitialsFromMatrix = (configToUpdate: ModelConfiguration) => {
	const newInits = configToUpdate.configuration?.semantics.ode.initials;
	if (newInits) {
		emit('update-initial', newInits, true);
	}
};

// When output changes via the dropdown, force the editor to update
watch(
	[() => props.initials, () => props.parameters],
	() => {
		const model = _.cloneDeep(props.model);
		if (model.semantics) {
			model.semantics.ode.initials = props.initials;
			model.semantics.ode.parameters = props.parameters;
			basicModelConfig.value = {
				id: '',
				name: '',
				modelId: props.model.id,
				configuration: model
			};
		}

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

.distribution-section {
	align-items: center;
	display: flex;
	flex-direction: row;
	gap: var(--gap);
}
</style>
