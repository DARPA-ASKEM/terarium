<template>
	<section>
		<img src="@assets/svg/plants.svg" alt="" draggable="false" />
		Configure in the side panel
	</section>
</template>

<script setup lang="ts"></script>

<style scoped>
section {
	align-items: center;
	display: flex;
	flex-direction: column;
	margin-bottom: 1rem;
	gap: 0.5rem;
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

img {
	height: 25%;
	width: 25%;
}
</style>
