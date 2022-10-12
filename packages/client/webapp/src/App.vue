<script setup lang="ts">
import Header from '@/components/Header.vue';
import Sidebar from '@/components/Sidebar.vue';
</script>

<template>
	<Header class="header" />
	<main>
		<Sidebar class="sidebar" />
		<router-view class="page" />
	</main>
</template>

<style scoped>
.header {
	z-index: 2;
}

main {
	display: flex;
	flex-grow: 1;
	isolation: isolate;
	z-index: 1;
}

.sidebar {
	z-index: 2;
}

.page {
	z-index: 1;
}
</style>
