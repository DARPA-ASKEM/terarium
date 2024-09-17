<template>
	<main>
		<slot />
	</main>
</template>

<style scoped>
/**FIXME: Moved these rules from tera-drilldown, Double check if this is really all needed, seems overkill */
main {
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
	overflow: hidden;
	flex-grow: 1;
}

main > :deep(*) {
	overflow: hidden;
}
</style>
