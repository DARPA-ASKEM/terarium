<template>
	<main>
		<section>
			<header>
				<h5>Code</h5>
				<Dropdown />
				<Button label="Add code block" icon="pi pi-plus" />
			</header>
			<ul>
				<li>Code block 1</li>
			</ul>
			<footer>
				<span><label>Model framework:</label><Dropdown /></span>
				<Button label="Run" icon="pi pi-play" severity="secondary" outlined size="large" />
			</footer>
		</section>
		<section class="preview">
			<h5>Preview</h5>
		</section>
	</main>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import Button from 'primevue/button';
</script>

<style scoped>
main {
	margin: 1rem;
	display: flex;
	gap: 2rem;
}

main > section {
	flex: 1;
}

header,
footer {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

footer {
	margin-top: auto;
}

.preview {
}

header > * {
	max-height: 40px;
}

ul {
	list-style: none;
}
</style>
