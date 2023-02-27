<template>
	<iframe title="Theia demo" :src="iframeSrc"></iframe>
</template>

<script setup lang="ts">
const iframeSrc = 'https://theia-ide.org/';
</script>

<style scoped>
iframe {
	border: none;
	display: block;
	min-height: 100%;
	min-width: 100%;
}
</style>
