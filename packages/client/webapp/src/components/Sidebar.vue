<script setup lang="ts"></script>

<template>
	<nav>
		<ul>
			<li>1</li>
			<li>2</li>
			<li>3</li>
		</ul>
		<ul>
			<li>4</li>
			<li>5</li>
		</ul>
	</nav>
</template>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: calc(100% - 4rem);
	position: fixed;
	width: 4rem;
	background-color: lightgreen;
}

ul {
	list-style: none;
	padding: 0;
	margin: 0;
}

li {
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	height: 3rem;
	width: 3rem;
	margin: 0.5rem auto;
	border-radius: 5px;
	background-color: whitesmoke;
	color: black;
}

li:hover {
	font-weight: bold;
	background-color: white;
}
</style>
