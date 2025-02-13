<template>
	<section class="asset-card">
		<slot />
	</section>
</template>
<script setup lang="ts"></script>

<style scoped>
.asset-card {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border);
	background-color: var(--surface-ground);
}
</style>
