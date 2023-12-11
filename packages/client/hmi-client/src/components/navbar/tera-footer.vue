<template>
	<section>
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
		<nav>
			<a target="_blank" rel="noopener noreferrer" @click="isAboutModalVisible = true">About</a>
			<a target="_blank" rel="noopener noreferrer" :href="documentation">Documentation</a>
			<a target="_blank" rel="noopener noreferrer" href="https://terarium.canny.io/report-an-issue">
				Report an issue
			</a>
			<a
				target="_blank"
				rel="noopener noreferrer"
				href="https://terarium.canny.io/request-a-feature"
			>
				Request a feature
			</a>
		</nav>
		<Teleport to="body">
			<tera-modal
				v-if="isAboutModalVisible"
				@modal-mask-clicked="isAboutModalVisible = false"
				@modal-enter-press="isAboutModalVisible = false"
			>
				<template #header>
					<h4>About Terarium</h4>
				</template>
				<article>
					<img
						src="@/assets/svg/terarium-logo.svg"
						alt="Terarium logo"
						class="about-terarium-logo"
					/>
					<p>
						Terarium is a comprehensive modeling and simulation platform designed to help
						researchers and analysts find models in academic literature, parameterize and calibrate
						them, run simulations to test a variety of scenarios, and analyze the results.
					</p>
				</article>
				<article>
					<img
						src="@/assets/svg/uncharted-logo-official.svg"
						alt="Uncharted Software logo"
						class="about-uncharted-logo"
					/>
					<p>
						Uncharted Software provides design, development and consulting services related to data
						visualization and analysis software.
					</p>
				</article>
				<template #footer>
					<div class="modal-footer">
						<p>&copy; Copyright Uncharted Software {{ new Date().getFullYear() }}</p>
						<Button class="p-button" @click="isAboutModalVisible = false">Close</Button>
					</div>
				</template>
			</tera-modal>
		</Teleport>
	</section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';

const isAboutModalVisible = ref(false);

const documentation = computed(() => {
	const host = window.location.hostname ?? 'localhost';
	if (host === 'localhost') {
		return '//localhost:8000';
	}
	const url = host.replace(/\bapp\b/g, 'documentation');
	return `https://${url}`;
});
</script>

<style scoped>
section {
	align-items: center;
	background-color: var(--surface-section);
	border-top: 1px solid var(--surface-border-light);
	display: flex;
	gap: 2rem;
	height: 3rem;
	justify-content: space-between;
}

nav {
	font-size: var(--font-caption);
	margin: 0 2rem;
	display: flex;
	align-items: center;
	justify-content: space-around;
	gap: 2rem;
}

a {
	text-decoration: none;
}

p {
	max-width: 40rem;
}

article > img {
	margin: 1rem 0;
}

.about-terarium-logo {
	width: 20rem;
}

.about-uncharted-logo {
	width: 10rem;
	margin-top: 3rem;
}

.modal-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}
</style>
