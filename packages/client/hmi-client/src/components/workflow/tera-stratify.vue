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
		<section v-if="stratifyView === StratifyView.Input">
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
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';

enum StratifyView {
	Input,
	Output
}
const stratifyView = ref(StratifyView.Input);
const stratifyStep = ref(1);
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	height: 100%;
	width: 100%;
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
</style>
