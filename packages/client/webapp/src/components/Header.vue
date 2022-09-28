<script setup lang="ts"></script>

<template>
	<nav>
		<header>TERArium</header>
		<button type="button"><a href="/logout">Logout</a></button>
	</nav>
</template>

<style scoped>
nav {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1px solid lightgrey;
	padding: 0.5rem 1rem;
}

header {
	font-weight: bold;
	font-size: 2rem;
}

a {
	color: white;
	text-decoration: none;
}
</style>
