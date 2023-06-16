<template>
	<main>
		<header>
			<h4>Stratify</h4>
			<span class="p-buttonset">
				<Button
					class="p-button-secondary p-button-sm"
					label="Input"
					icon="pi pi-sign-in"
					@click="stratifyView = StratifyView.Input"
					:active="stratifyView === StratifyView.Input"
				/>
				<Button
					class="p-button-secondary p-button-sm"
					label="Output"
					icon="pi pi-sign-out"
					@click="stratifyView = StratifyView.Output"
					:active="stratifyView === StratifyView.Output"
				/>
			</span>
		</header>
		<section v-if="stratifyView === StratifyView.Input" class="input-view">
			<nav>
				<div class="step-header" :active="stratifyStep === 1">
					<h5>Step 1</h5>
					Define strata
				</div>
				<div class="step-header" :active="stratifyStep === 2">
					<h5>Step 2</h5>
					Assign types
				</div>
				<div class="step-header" :active="stratifyStep === 3">
					<h5>Step 3</h5>
					Manage interactions
				</div>
			</nav>
			<section class="step-1">
				<div class="instructions">Define the groups you want to stratify your model with.</div>
				<Accordion :active-index="0">
					<AccordionTab header="Model">
						<div class="step-1-inner">
							<tera-model-diagram :model="null" :is-editable="false" />
							<div class="input">
								<label for="strata-type">Select a strata type</label>
								<Dropdown
									id="strata-type"
									v-model="strataType"
									:options="['Age groups', 'Location-travel']"
								/>
							</div>
							<div class="input">
								<label for="labels"
									>Enter a comma separated list of labels for each group. (Max 100)</label
								>
								<Textarea id="labels" v-model="labels" />
								<span><i class="pi pi-info-circle" />Or drag a CSV file into this box</span>
							</div>
							<div class="buttons">
								<Button
									class="p-button-secondary p-button-sm"
									label="Add another strata group"
									icon="pi pi-plus"
								/>
								<Button class="p-button-secondary p-button-sm" label="Generate strata" />
							</div>
						</div>
					</AccordionTab>
				</Accordion>
			</section>
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import TeraModelDiagram from '../models/tera-model-diagram.vue';

enum StratifyView {
	Input,
	Output
}
const stratifyView = ref(StratifyView.Input);
const stratifyStep = ref(1);
const strataType = ref();
const labels = ref();
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	height: 100%;
	width: 100%;
	overflow-y: scroll;
}

header {
	display: flex;
	gap: 1rem;
	padding: 1rem;
	align-items: center;
}

nav {
	padding-left: 1rem;
	display: flex;
	margin-bottom: 1rem;
}

.step-header {
	display: flex;
	flex-direction: column;
	color: var(--primary-color-dark);
	font-size: var(--font-body-small);
	padding: 1rem;
	border-radius: 8px;
}

.step-header[active='true'] {
	background-color: var(--surface-highlight);
}

.step-header h5 {
	color: var(--text-color-primary);
}

.step-1 {
	padding-left: 0.5rem;
}

.step-1 > div:first-of-type {
	padding-left: 0.5rem;
	margin-bottom: 1rem;
}

.input {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.input i {
	margin-right: 0.5rem;
}

.input span {
	color: var(--text-color-subdued);
	display: flex;
	align-items: center;
}

.step-1-inner {
	display: flex;
	gap: 1rem;
	flex-direction: column;
}

#strata-type {
	width: 50%;
}

.buttons {
	display: flex;
	justify-content: space-between;
}
</style>
