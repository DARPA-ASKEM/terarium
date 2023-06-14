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
				<TabView>
					<TabPanel :disabled="true">
						<template #header>
							<div class="step-header">
								<h5>Step 1</h5>
								Define strata
							</div>
						</template>
					</TabPanel>
					<TabPanel :disabled="true">
						<template #header>
							<div class="step-header">
								<h5>Step 2</h5>
								Assign types
							</div>
						</template>
					</TabPanel>
					<TabPanel :disabled="true">
						<template #header>
							<div class="step-header">
								<h5>Step 3</h5>
								Manage interactions
							</div>
						</template>
					</TabPanel>
				</TabView>
			</nav>
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';

enum StratifyView {
	Input,
	Output
}
const stratifyView = ref(StratifyView.Input);
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
}

.step-header {
	display: flex;
	flex-direction: column;
	color: var(--primary-color-dark);
	font-size: var(--font-body-small);
}

.step-header h5 {
	color: var(--text-color-primary);
}

:deep(.p-tabview .p-tabview-nav li.p-highlight .p-tabview-nav-link) {
	background-color: var(--surface-highlight);
}

li.p-highlight .step-header h5 {
	color: var(--primary-color-dark);
}

:deep(.p-tabview .p-tabview-nav .p-tabview-ink-bar) {
	visibility: hidden;
}

:deep(.p-tabview .p-tabview-nav li .p-tabview-nav-link) {
	border-radius: 8px;
}

:deep(.p-disabled, .p-component:disabled) {
	opacity: 100;
}
</style>
