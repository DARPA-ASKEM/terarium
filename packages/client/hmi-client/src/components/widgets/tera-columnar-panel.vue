<template>
	<main>
		<slot />
	</main>
</template>

<style scoped>
main {
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	overflow: hidden;
	gap: var(--gap-small);
	flex-grow: 1;
}

main > :deep(*) {
	display: grid;
	height: 100%;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	gap: var(--gap-small);
	overflow: hidden;
}
</style>
