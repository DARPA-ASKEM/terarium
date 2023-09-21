<script setup lang="ts">
import Button from 'primevue/button';

/**
 * A modal with content slots for a header, body, and footer. Use v-if to control visiblity.
 * @example
 * <modal v-if="isModalVisible">
 * 		<template #header>Header content</template>
 * 		<template #default>Body content</template>
 * 		<template #footer>Footer content</template>
 * </modal>
 *
 * Clicking outside the modal will emit the event modalMaskClicked.
 * @example
 * <modal @modal-mask-clicked="closeModal"></modal>
 */

const emit = defineEmits(['modal-enter-press', 'modal-mask-clicked', 'on-close-clicked']);
</script>

<template>
	<Transition name="modal">
		<main @keyup.enter="emit('modal-enter-press')">
			<section class="fullscreen">
				<div>
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
				</div>
			</section>
			<aside @click.self="emit('modal-mask-clicked')" />
		</main>
	</Transition>
</template>

<style scoped>
main {
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
	padding: 1.5rem 2rem;
}

footer {
	display: flex;
	flex-direction: row-reverse;
	gap: 1rem;
	justify-content: end;
	margin-top: 2rem;
	padding: 1.5rem 2rem;
}

.modal-enter-from,
.modal-leave-to {
	opacity: 0;
}

.modal-enter-from main > section,
.modal-leave-to main > section {
	-webkit-transform: scale(0.9);
	transform: scale(0.9);
}
</style>
