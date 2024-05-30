<template>
	<h6><slot /></h6>
</template>

<style scoped>
h6 {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	text-overflow: ellipsis;
	overflow: hidden;
	-webkit-line-clamp: 3;
	font-weight: var(--font-weight-semibold);
}

h6:empty {
	display: none;
}
</style>
