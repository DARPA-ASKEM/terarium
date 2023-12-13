<template>
	<section class="container">
		<section class="card">
			<div class="draggable"></div>
			<main>
				<header>Template title</header>
				<section>Diagram/Equations</section>
			</main>
		</section>
		<ul>
			<li v-for="(variable, index) in fakeVariables" :key="index">
				{{ variable }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
const fakeVariables = ['X', 'Y', 'p'];
</script>

<style scoped>
.container {
	display: flex;
	font-size: var(--font-caption);

	& ul {
		list-style: none;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		margin: 0.75rem 0;

		& > li {
			background-color: var(--surface-section);
			border: 1px solid var(--surface-border-alt);
			border-top-right-radius: var(--border-radius);
			border-bottom-right-radius: var(--border-radius);
			padding: 0.15rem 0.25rem;
		}
	}
}

.card {
	display: flex;
	background-color: var(--surface-section);
	border-radius: var(--border-radius-medium);
	outline: 1px solid var(--surface-border-alt);

	& > main {
		margin: 0.5rem;
		display: flex;
		flex-direction: column;
		gap: 1rem;

		& > * {
			margin: 0 auto;
		}
	}
}

.draggable {
	width: 0.75rem;
	border-top-left-radius: var(--border-radius-medium);
	border-bottom-left-radius: var(--border-radius-medium);
	background-color: var(--surface-highlight);
}
</style>
