<script setup lang="ts">
import Button from 'primevue/button';

const emit = defineEmits(['modal-enter-press', 'modal-mask-clicked', 'on-close-clicked']);
</script>

<template>
	<div class="overlay-container" @keyup.enter="emit('modal-enter-press')">
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
					size="large"
					@click="emit('on-close-clicked')"
				/>
			</header>
			<section class="content"><slot /></section>
		</section>
	</div>
</template>

<style scoped>
.overlay-container {
	isolation: isolate;
	overflow-y: auto;
	z-index: var(--z-index, var(--z-index-modal));
	position: fixed;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.32);
}

.overlay-container > section {
	height: fit-content;
	min-height: 98%;
	width: 98vw;
	margin-top: 1%;
	margin-bottom: 1%;
	margin-left: 1%;
	background: #fff;
	border-radius: 0.5rem;
}
/* main {
	isolation: isolate;
	z-index: var(--z-index, var(--z-index-modal));
}

main > * {
	position: absolute;
}
aside {
	z-index: 1;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	transition: opacity 0.1s ease;
}

main > section {
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	margin: 0px auto;
	transition: all 0.1s ease;
	z-index: 2;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	overflow: hidden;
	display: flex;
	flex-direction: column;
	height: 100%;
	width: 100%;
	border-radius: 0.5rem;
	background-color: transparent;
	overflow-y: auto;
}

main > section > div {
	width: 98vw;
	background-color: #fff;
	margin: 1%;
	border-radius: 0.5rem;
} */

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
	padding: 1.5rem 2rem;
}

/* .modal-enter-from,
.modal-leave-to {
	opacity: 0;
}

.modal-enter-from main > section,
.modal-leave-to main > section {
	-webkit-transform: scale(0.9);
	transform: scale(0.9);
} */
</style>
