<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div :tabName="FunmanTabs.Wizard">
			<tera-drilldown-section>
				<main>
					<h4 class="primary-text">Set validation parameters <i class="pi pi-info-circle" /></h4>
					<p class="secondary-text">
						The validator will use these parameters to execute the sanity checks.
					</p>
					<div class="section-row timespan">
						<div class="button-column">
							<label>Start time</label>
							<InputNumber inputId="integeronly" v-model="startTime" />
						</div>
						<div class="button-column">
							<label>End time</label>
							<InputNumber inputId="integeronly" v-model="endTime" />
						</div>
						<div class="button-column">
							<label>Number of steps</label>
							<InputNumber inputId="integeronly" v-model="numberOfSteps" />
						</div>
					</div>
					<InputText
						:disabled="true"
						class="p-inputtext-sm timespan-list"
						inputId="integeronly"
						v-model="requestStepListString"
					/>
					<p v-if="!showAdditionalOptions" @click="toggleAdditonalOptions" class="green-text">
						Show additional options
					</p>
					<p v-if="showAdditionalOptions" @click="toggleAdditonalOptions" class="green-text">
						Hide additional options
					</p>
					<div v-if="showAdditionalOptions">
						<div class="button-column">
							<label>Tolerance</label>
							<InputNumber
								mode="decimal"
								:min="0"
								:max="1"
								:min-fraction-digits="0"
								:max-fraction-digits="7"
								v-model="tolerance"
							/>
						</div>
						<Slider v-model="tolerance" :min="0" :max="1" :step="0.01" />
						<div class="section-row">
							<!-- This will definitely require a proper tool tip. -->
							<label>Select parameters to synthesize <i class="pi pi-info-circle" /></label>
							<div
								v-for="(parameter, index) of requestParameters"
								:key="index"
								class="button-column"
							>
								<label>{{ parameter.name }}</label>
								<Dropdown v-model="parameter.label" :options="labelOptions"> </Dropdown>
							</div>
						</div>
					</div>
					<div class="spacer">
						<h4>Add sanity checks</h4>
						<p>Model configurations will be tested against these constraints</p>
					</div>
					<tera-constraint-group-form
						v-for="(cfg, index) in node.state.constraintGroups"
						:key="index"
						:config="cfg"
						:index="index"
						:model-node-options="modelNodeOptions"
						@delete-self="deleteConstraintGroupForm"
						@update-self="updateConstraintGroupForm"
					/>

					<Button label="Add another constraint" size="small" @click="addConstraintForm" />
				</main>
			</tera-drilldown-section>
		</div>
		<div :tabName="FunmanTabs.Notebook">
			<tera-drilldown-section>
				<main>
					<!-- TODO: notebook functionality -->
					<p>{{ requestConstraints }}</p>
				</main>
			</tera-drilldown-section>
		</div>

		<template #preview>
			<tera-drilldown-preview title="Validation results">
				<tera-funman-output v-if="outputId" :fun-model-id="outputId" />
				<div v-else>
					<img src="@assets/svg/plants.svg" alt="" draggable="false" />
					<h4>No Output</h4>
				</div>
				<p>
					Lorem ipsum dolor sit amet. Sed accusantium dicta qui laborum asperiores eos voluptatem
					ducimus et deleniti consequatur. Qui ipsam quia qui sunt voluptatem qui animi molestiae
					nam expedita distinctio. Et perferendis consequatur ab doloribus quod quo suscipit
					praesentium rem quam vero vel itaque porro.
				</p>
				<p>
					Ut architecto magni sed vero inventore et beatae perspiciatis 33 internos harum et illo
					quidem. Eum fugit blanditiis qui recusandae velit aut soluta placeat.
				</p>
				<p>
					Non deleniti quisquam in laboriosam facilis aut laboriosam quos ex animi doloribus in quia
					animi sit omnis nulla sed ducimus repellat! Et asperiores nobis vel voluptas dolorem hic
					mollitia nesciunt non cupiditate distinctio qui dolores natus. Est sequi voluptatem ea
					beatae incidunt eum dolorem tempora cum dolores mollitia id eveniet aliquid.
				</p>
				<p>
					A possimus tempora qui galisum soluta aut sint porro nam veniam illum. Et excepturi
					consequatur est libero sunt ea quia suscipit et eaque fugit. Sit accusamus sint ea omnis
					vitae aut incidunt dolor.
				</p>
				<p>
					Hic dolorem iste est iste unde quo incidunt maiores. Et exercitationem sunt est earum enim
					in quas illo. Et aperiam eveniet aut possimus deserunt aut suscipit exercitationem sit
					reiciendis quos et magni libero aut animi beatae. Et minus nostrum ab odio praesentium sed
					sunt laudantium ex cupiditate debitis qui quaerat modi aut nobis dignissimos.
				</p>
				<p>
					Et quia similique ex impedit quam qui harum quaerat aut dolorem cumque ab exercitationem
					omnis id enim amet ea obcaecati rerum. Sed quasi dolorem ea minus expedita rem adipisci
					voluptates eum explicabo error non molestiae aperiam. Eos harum consequatur vel quia nemo
					et adipisci voluptatem et debitis doloremque. Et ullam esse qui numquam libero rem
					possimus velit.
				</p>
				<p>
					Non itaque illo quo facilis maiores aut consequatur molestias vel porro praesentium. 33
					reiciendis rerum sed nihil rerum sit corporis dolore non labore ipsam eos mollitia
					suscipit et ratione fugiat.
				</p>
				<p>
					Qui sunt voluptas aut maiores deleniti et accusantium excepturi sit omnis fuga! Sed dicta
					aliquid et fugiat temporibus hic ullam natus est numquam dolore.
				</p>
				<p>
					Non minus esse est molestias optio ut incidunt doloribus. Sit atque dolorum est tempore
					voluptate sit dicta sint et consequatur alias At nulla officiis aut earum eveniet qui
					maiores galisum. Vel ducimus architecto nam quia necessitatibus eum quaerat enim aut
					voluptatem impedit et odit magnam qui molestiae atque ea odio cumque. Est mollitia fugiat
					eum aliquam natus vel commodi rerum et animi repudiandae et ipsa quisquam!
				</p>
				<p>
					Ut esse maxime ad blanditiis voluptatibus qui nostrum voluptas et fuga nihil eos ipsam
					nobis ut maiores dolores ut placeat consequatur. Et nesciunt quam sed suscipit voluptatem
					non assumenda voluptatem sed quisquam vitae et reiciendis velit et suscipit quidem?
				</p>
				<p>
					Qui molestiae quod nam officiis sint ut repudiandae minima est cumque galisum eum facilis
					enim hic nemo animi At deserunt expedita. Ut error consequatur 33 dolor nihil et quia
					ratione! Non eaque omnis et blanditiis eius eum voluptate aperiam et velit sint ea galisum
					iusto ut voluptas odio!
				</p>
				<p>
					Est odio sunt hic sunt illo non tempore ratione quo sunt facilis et sapiente error qui
					facere mollitia. Et asperiores voluptatem et deleniti asperiores eum omnis dolorem a
					perspiciatis fugiat.
				</p>
				<p>
					Ut voluptas officiis ut veniam voluptate et dolor velit est suscipit fugit ea blanditiis
					expedita et temporibus impedit qui consequuntur consequuntur. Sit fuga perspiciatis qui
					quia neque et maiores iste sit repudiandae dolor qui odit consequuntur. Vel galisum facere
					aut omnis possimus hic distinctio optio sed doloremque molestiae. Ut sapiente consequuntur
					ea deleniti quaerat est nisi obcaecati hic dolorum voluptas ea enim consequuntur qui
					eveniet perferendis et asperiores consequatur.
				</p>
				<p>
					Vel reiciendis rerum sed aspernatur architecto et repudiandae modi cum consequatur sequi
					qui tempore voluptas cum odio quidem. Et quia eligendi est aliquid nostrum et soluta
					molestiae qui voluptate omnis eum aliquam fugiat quo alias asperiores! Aut saepe dolor ut
					quis fugit et quas voluptas. Eos labore dolor et quae nihil vel ipsa sapiente eum
					repudiandae magni.
				</p>
				<p>
					Vel fugit suscipit 33 laborum sunt ut velit quia. Et expedita maiores est nemo doloribus
					qui mollitia quaerat sed rerum nostrum ab esse voluptatem rem deleniti sequi.
				</p>
				<p>
					Qui reiciendis deserunt in tempore obcaecati est autem officia non architecto vitae aut
					delectus error. Id maiores dignissimos sed soluta debitis aut fugit veritatis cum adipisci
					dolorum est maxime galisum ad modi quia ex tempore sunt. In enim culpa aut odit temporibus
					vel internos similique id voluptatem amet est libero iusto. Est veritatis galisum ad
					accusantium rerum ut nemo dolores aut tempora deserunt ut iste quisquam ad quaerat ratione
					hic odit aperiam.
				</p>
				<p>
					Ut laboriosam quae et molestiae minima cum excepturi tempore ea dolores expedita ut quia
					rerum. Aut fuga quidem vel eligendi tempora est perferendis cupiditate. Eos quam quisquam
					ut praesentium vitae cum obcaecati fuga et debitis recusandae. Est placeat omnis et minima
					laborum est tempore dolores et animi itaque et dignissimos inventore.
				</p>
				<p>
					Et dolor culpa aut dolorum omnis qui neque neque. Sit voluptatem minima eos perspiciatis
					incidunt ut architecto repudiandae et quia corrupti est dolore ipsum ut eius vitae. Est
					voluptate voluptate non labore quia et expedita laudantium. Ea minus voluptate non nisi
					delectus non quis tempore qui voluptas internos ex natus galisum.
				</p>
				<p>
					Est dolorem modi et quae quia non porro rerum qui sint Quis. Est ratione vero est
					molestiae quas et exercitationem porro! Et quasi saepe et culpa nulla id aspernatur
					perferendis et illum odit. Ab neque recusandae At animi praesentium hic enim quas ad
					veniam ipsum est accusantium maxime sed vero consequatur.
				</p>
				<p>
					Sit nihil molestiae eos veniam amet et necessitatibus iste. Ut dignissimos illum ut sequi
					minus sit voluptates vitae. Et modi praesentium rem autem iusto et excepturi nihil a
					assumenda placeat et temporibus dolorem et quaerat nisi ut distinctio dolorem! Non quia
					veritatis sed reiciendis nobis aut voluptas voluptatem id porro sapiente ea sint nemo!
				</p>
			</tera-drilldown-preview>
		</template>

		<template #footer>
			<Button
				outlined
				:loading="showSpinner"
				class="run-button"
				label="Run"
				icon="pi pi-play"
				@click="runMakeQuery"
			/>
			<Button outlined label="Save as a new model" />
			<Button label="Close" @click="emit('close')" />
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import _, { floor } from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Dropdown from 'primevue/dropdown';
import Slider from 'primevue/slider';
import { FunmanPostQueriesRequest, Model, ModelConfiguration } from '@/types/Types';
import { getQueries, makeQueries } from '@/services/models/funman-service';
import { WorkflowNode } from '@/types/workflow';
import teraConstraintGroupForm from '@/components/funman/tera-constraint-group-form.vue';
import teraFunmanOutput from '@/components/funman/tera-funman-output.vue';
import { getModelConfigurationById } from '@/services/model-configurations';
import { getModel } from '@/services/model';
import { useToastService } from '@/services/toast';
import { v4 as uuidv4 } from 'uuid';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { FunmanOperationState, ConstraintGroup, FunmanOperation } from './funman-operation';

const props = defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();

const emit = defineEmits(['append-output-port', 'update-state', 'close']);

enum FunmanTabs {
	Wizard = 'Wizard',
	Notebook = 'Notebook'
}
const toast = useToastService();

const labelOptions = ['any', 'all'];
const showSpinner = ref(false);
const showAdditionalOptions = ref(false);
const tolerance = ref(props.node.state.tolerance);
const startTime = ref(props.node.state.currentTimespan.start);
const endTime = ref(props.node.state.currentTimespan.end);
const numberOfSteps = ref(props.node.state.numSteps);
const requestStepList = computed(() => getStepList());
const requestStepListString = computed(() => requestStepList.value.join()); // Just used to display. dont like this but need to be quick
const requestConstraints = computed(() =>
	// Same as node state's except typing for state vs linear constraint
	props.node.state.constraintGroups?.map((ele) => {
		if (ele.variables.length === 1) {
			// State Variable Constraint
			return {
				name: ele.name,
				variable: ele.variables[0],
				interval: ele.interval,
				timepoints: ele.timepoints
			};
		}
		return {
			// Linear Constraint
			name: ele.name,
			variables: ele.variables,
			weights: ele.weights,
			interval: ele.interval,
			timepoints: ele.timepoints
		};
	})
);
const requestParameters = ref();
const model = ref<Model | null>();
const modelConfiguration = ref<ModelConfiguration>();
const modelNodeOptions = ref<string[]>([]); // Used for form's multiselect.
const outputId = computed(() => {
	if (props.node.outputs[0]?.value) return String(props.node.outputs[0].value);
	return undefined;
});

function toggleAdditonalOptions() {
	showAdditionalOptions.value = !showAdditionalOptions.value;
}

const runMakeQuery = async () => {
	if (!model.value) {
		toast.error('', 'No Model provided for request');
		return;
	}

	const request: FunmanPostQueriesRequest = {
		model: model.value,
		request: {
			constraints: requestConstraints.value,
			parameters: requestParameters.value,
			structure_parameters: [
				{
					name: 'schedules',
					schedules: [
						{
							timepoints: requestStepList.value
						}
					]
				}
			],
			config: {
				use_compartmental_constraints: true,
				normalization_constant: 1,
				tolerance: tolerance.value
			}
		}
	};

	const response = await makeQueries(request); // Just commented out so i do not break funman
	getStatus(response.id);
};

// TODO: Poller? https://github.com/DARPA-ASKEM/terarium/issues/2196
const getStatus = async (runId) => {
	showSpinner.value = true;
	const response = await getQueries(runId);
	if (response?.error === true) {
		showSpinner.value = false;
		toast.error('', 'An error occured Funman');
		console.log(response);
	} else if (response?.done === true) {
		showSpinner.value = false;
		updateOutputPorts(runId);
	} else {
		setTimeout(async () => {
			getStatus(runId);
		}, 2000);
	}
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		id: uuidv4(),
		label: `${portLabel} Result`,
		type: FunmanOperation.outputs[0].type,
		value: runId
	});
};

const addConstraintForm = () => {
	const state = _.cloneDeep(props.node.state);
	const newGroup: ConstraintGroup = {
		borderColour: '#00c387',
		name: '',
		timepoints: { lb: 0, ub: 100 },
		variables: []
	};
	if (!state.constraintGroups) {
		state.constraintGroups = [];
	}
	state.constraintGroups.push(newGroup);

	emit('update-state', state);
};

const deleteConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.constraintGroups) {
		return;
	}
	state.constraintGroups.splice(data.index, 1);

	emit('update-state', state);
};

const updateConstraintGroupForm = (data) => {
	const state = _.cloneDeep(props.node.state);
	if (!state.constraintGroups) {
		return;
	}
	state.constraintGroups[data.index] = data.updatedConfig;

	emit('update-state', state);
};

// Used to set requestStepList.
// Grab startTime, endTime, numberOfSteps and create list.
function getStepList() {
	const aList = [startTime.value];
	const stepSize = floor((endTime.value - startTime.value) / numberOfSteps.value);
	for (let i = 1; i < numberOfSteps.value; i++) {
		aList[i] = i * stepSize;
	}
	aList.push(endTime.value);
	return aList;
}

const setModelOptions = async () => {
	const modelConfigurationId = props.node.inputs[0].value?.[0];
	if (modelConfigurationId) {
		modelConfiguration.value = await getModelConfigurationById(modelConfigurationId);
		if (modelConfiguration.value) {
			model.value = await getModel(modelConfiguration.value.modelId);
			const modelColumnNameOptions: string[] =
				modelConfiguration.value.configuration.model.states.map((state) => state.id);
			// observables are not currently supported
			// if (modelConfiguration.value.configuration.semantics?.ode?.observables) {
			// 	modelConfiguration.value.configuration.semantics.ode.observables.forEach((o) => {
			// 		modelColumnNameOptions.push(o.id);
			// 	});
			// }
			modelNodeOptions.value = modelColumnNameOptions;

			if (model.value && model.value.semantics?.ode.parameters) {
				setRequestParameters(model.value.semantics?.ode.parameters);
			} else {
				toast.error('', 'Provided model has no parameters');
			}
		}
	}
};

const setRequestParameters = async (modelParameters) => {
	requestParameters.value = modelParameters.map((ele) => {
		if (ele.distribution) {
			return {
				name: ele.id,
				interval: {
					lb: ele.distribution.parameters.minimum,
					ub: ele.distribution.parameters.maximum
				},
				label: 'any'
			};
		}

		return {
			name: ele.id,
			interval: {
				lb: ele.value,
				ub: ele.value
			},
			label: 'any'
		};
	});
};

// Set model, modelConfiguration, modelNodeOptions
watch(
	() => props.node.inputs[0],
	async () => {
		setModelOptions();
	},
	{ immediate: true }
);
</script>

<style scoped>
.primary-text {
	color: var(--Text-Primary, #020203);
	/* Body Medium/Semibold */
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem; /* 150% */
	letter-spacing: 0.03125rem;
}

.secondary-text {
	color: var(--Text-Secondary, #667085);
	/* Body Small/Regular */
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}

.button-column {
	display: flex;
	flex-direction: column;
	padding: 1rem 0rem 0.5rem 0rem;
	align-items: flex-start;
	align-self: stretch;
}

.section-row {
	display: flex;
	padding: 0.5rem 0rem;
	align-items: center;
	gap: 0.8125rem;
	align-self: stretch;
}

.timespan > .button-column {
	width: 100%;
}

div.section-row.timespan > div > span {
	width: 100%;
}

.timespan-list {
	width: 100%;
}

.spacer {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.green-text {
	color: var(--Primary, #1b8073);
}
.green-text:hover {
	color: var(--text-color-subdued);
}

.run-button {
	margin-right: auto;
}
</style>
