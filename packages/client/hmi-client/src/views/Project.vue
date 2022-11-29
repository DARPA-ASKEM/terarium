<script setup lang="ts">
import { Project } from '@/types/Project';

defineProps<{
	project: Project;
}>();
</script>

<template>
	<div class="flex-container">
		<header>
			<h2>{{ project?.name }}</h2>
			<p class="secondary-text">Last updated: {{ project?.timestamp }}</p>
		</header>
		<section class="content-container">
			<section class="summary">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<section class="description">
						<!-- Author -->
						<section class="author">Edwin Lai, Yohann Paris</section>
						<p>
							{{ project?.description }}
						</p>
					</section>
					<section class="related-projects">
						<h4>Related projects:</h4>
					</section>
				</div>
			</section>
			<section class="detail">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<section>
						<h4>Questions / Simulation Plans</h4>
						<p>
							Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
							incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
							exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
							dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
							Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
							mollit anim id est laborum.
						</p>
					</section>
					<section>
						<h4>Models</h4>
						<p>
							Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
							incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
							exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
							dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
							Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
							mollit anim id est laborum.
						</p>
					</section>
					<section>
						<h4>Scenarios</h4>
						<p>
							Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
							incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
							exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
							dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
							Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
							mollit anim id est laborum.
						</p>
					</section>
					<section>
						<h4>Key Outcomes</h4>
						<p>
							Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
							incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
							exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure
							dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
							Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
							mollit anim id est laborum.
						</p>
					</section>
				</div>
			</section>
		</section>
	</div>
</template>

<style scoped>
.flex-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	margin: 0.5rem;
	background: white;
}

.content-container {
	margin-left: 1rem;
	column-gap: 2rem;
	flex-direction: row;
}

header {
	margin: 1rem;
}

section {
	display: flex;
	width: 100%;
	flex-direction: column;
}

.summary {
	flex: 0.25;
}

.author {
	line-height: 1.75rem;
}

.detail {
	flex: 0.75;
}

.summary section,
.detail section {
	margin: 1rem 0;
	display: block;
}

h4 {
	font: var(--un-font-h4);
	margin: 1rem 0;
}

h3 {
	font: var(--un-font-h3);
}

h2 {
	font: var(--un-font-h2);
}

.secondary-text {
	color: var(--un-color-body-text-secondary);
}
</style>
