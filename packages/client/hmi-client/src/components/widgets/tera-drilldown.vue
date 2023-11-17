<template>
	<aside class="overlay-container">
		<section>
			<header ref="header">
				<div>
					<slot name="header" />
				</div>
				<Button
					icon="pi pi-times"
					text
					rounded
					aria-label="Close"
					@click="emit('on-close-clicked')"
				/>
			</header>
			<main><slot /></main>
		</section>
	</aside>
</template>

<script setup lang="ts">
import Button from 'primevue/button';

const emit = defineEmits(['on-close-clicked']);
</script>

<style scoped>
.overlay-container {
	isolation: isolate;
	z-index: var(--z-index, var(--z-index-modal));
	position: fixed;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.32);
}

/* There is a performance issue with these large modals. 
When scrolling it takes time to render the content, paticularly heavy content such as the LLM integrations. This will show
us the main application behind the modal temporarily as content loads when scrolling which is a bit of an eye sore.
An extra div here is used to alleviate the impact of these issues a little by allowing us to see the overlay container rather
than the main application behind the modal when these render issues come, however this is still an issue regardless.
*/
.overlay-container > section {
	height: calc(100vh - 1rem);
	margin: 0.5rem;
	background: #fff;
	border-radius: var(--modal-border-radius);
	overflow: hidden;
}

header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
	background-color: var(--surface-highlight);
	padding: 20px 16px;
	height: 56px;
}

main {
	margin: 0 0 0.5rem;
	max-width: inherit;
	/* contentHeight = fullscreen - modalMargin - headerHeight*/
	height: calc(100vh - 1rem - 56px);
	display: flex;
	flex-direction: column;
}

.p-button.p-button-icon-only.p-button-rounded {
	height: 24px;
	width: 24px;
}
.p-button:deep(.p-button-icon) {
	font-size: 16px;
	color: var(--text-color-primary);
}
</style>
