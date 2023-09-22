<template>
	<aside class="overlay-container">
		<div>
			<section>
				<header ref="header">
					<div>
						<slot name="header" />
					</div>
					<Button
						class="close-button"
						icon="pi pi-times"
						text
						rounded
						aria-label="Close"
						@click="emit('on-close-clicked')"
					/>
				</header>
				<main class="content"><slot /></main>
			</section>
		</div>
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
.overlay-container > div {
	overflow-y: auto;
	width: 100%;
	height: 100%;
}

.overlay-container > div > section {
	height: fit-content;
	min-height: 98%;
	width: 98vw;
	margin-top: 1%;
	margin-bottom: 1%;
	margin-left: 1%;
	background: #fff;
	border-radius: 0.5rem;
	overflow: hidden;
}

.content {
	padding: 0 2rem;
	max-width: inherit;
}

header {
	margin-bottom: 1rem;
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
	background-color: var(--surface-highlight);
	padding: 0.5rem 1.5rem;
}
.close-button.p-button.p-button-icon-only {
	height: 4rem;
	width: 4rem;
}

.close-button.p-button.p-button-icon-only:deep(.p-button-icon) {
	font-size: 1.75rem;
}
</style>
