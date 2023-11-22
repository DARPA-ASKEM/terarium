<template>
	<h5><slot /></h5>
</template>

<style scoped>
h5 {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	text-overflow: ellipsis;
	overflow: hidden;
	-webkit-line-clamp: 3;
}

h5:empty {
	display: none;
}
</style>
