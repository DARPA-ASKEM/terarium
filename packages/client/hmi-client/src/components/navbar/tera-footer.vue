<template>
	<section>
		<img src="@assets/svg/uncharted-logo-dark.svg" alt="logo" class="ml-2" />
		<nav>
			<a target="_blank" rel="noopener noreferrer" href="https://terarium.canny.io/report-an-issue">
				Report an issue
			</a>
			<a target="_blank" rel="noopener noreferrer" href="https://terarium.canny.io/request-a-feature">
				Request a feature
			</a>
		</nav>
	</section>
</template>

<style scoped>
section {
	align-items: center;
	background-color: var(--surface-section);
	border-top: 1px solid var(--surface-border-light);
	display: flex;
	gap: 2rem;
	height: 3rem;
	justify-content: space-between;
}

nav {
	font-size: var(--font-caption);
	margin: 0 2rem;
	display: flex;
	align-items: center;
	justify-content: space-around;
	gap: 2rem;
}

a {
	text-decoration: none;
}

p {
	max-width: 40rem;
}

article > img {
	margin: 1rem 0;
}
</style>
