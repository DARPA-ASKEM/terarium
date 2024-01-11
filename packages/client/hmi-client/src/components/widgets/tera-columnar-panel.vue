<template>
	<main>
		<slot />
	</main>
</template>

<script setup lang="ts"></script>

<style scoped>
main {
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	overflow: hidden;
	gap: var(--gap-small);
}
</style>
