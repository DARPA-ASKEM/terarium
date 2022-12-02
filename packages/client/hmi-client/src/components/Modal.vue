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
</script>

<template>
	<Transition name="modal">
		<aside @click.self="$emit('modalMaskClicked')">
			<main>
				<slot name="header"></slot>
				<slot></slot>
				<slot name="footer"> </slot>
			</main>
		</aside>
	</Transition>
</template>

<style scoped>
aside {
	position: fixed;
	z-index: 9998;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	transition: opacity 0.1s ease;
}

main {
	background-color: #fff;
	border-radius: 2px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	margin: 0px auto;
	padding: 2rem;
	transition: all 0.1s ease;
	min-width: max-content;
	width: 30rem;
}

.modal-enter-from,
.modal-leave-to {
	opacity: 0;
}

.modal-enter-from main,
.modal-leave-to main {
	-webkit-transform: scale(0.9);
	transform: scale(0.9);
}
</style>
