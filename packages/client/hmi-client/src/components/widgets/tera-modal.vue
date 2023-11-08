<script setup lang="ts">
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

defineProps<{
	zIndex?: number;
}>();
</script>

<template>
	<Transition name="modal">
		<main :style="{ '--z-index': zIndex }" @keyup.enter="$emit('modal-enter-press')">
			<section>
				<header>
					<slot name="header" />
				</header>
				<section class="content"><slot /></section>
				<section><slot name="math-editor" /></section>
				<footer>
					<slot name="footer" />
				</footer>
			</section>
			<aside @click.self="$emit('modalMaskClicked')" />
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
	max-height: 95vh;
	max-width: 640px;
	background-color: #fff;
	border-radius: var(--modal-border-radius);
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	margin: 0px auto;
	padding: 2rem 0;
	transition: all 0.1s ease;
	min-width: max-content;
	width: 80vw;
	z-index: 2;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
}

.content {
	max-height: 65vh;
	padding: 0 2rem;
	overflow-y: auto;
}

header {
	margin-bottom: 1rem;
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

footer {
	display: flex;
	flex-direction: row-reverse;
	gap: 1rem;
	justify-content: end;
	margin-top: 2rem;
}

header,
footer {
	padding: 0 2rem;
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

.content:deep(label) {
	display: block;
	margin-bottom: 0.5em;
}

.content:deep(input),
.content:deep(textarea) {
	display: block;
	margin-bottom: 1rem;
	width: 100%;
}
</style>
