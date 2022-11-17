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
		<div class="modal-mask" @click.self="$emit('modalMaskClicked')">
			<div class="modal-container">
				<header>
					<slot name="header"></slot>
				</header>
				<main>
					<slot></slot>
				</main>
				<footer>
					<slot name="footer"> </slot>
				</footer>
			</div>
		</div>
	</Transition>
</template>

<style scoped>
.modal-mask {
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

.modal-container {
	width: max-content;
	margin: 0px auto;
	padding: 2rem;
	background-color: #fff;
	border-radius: 2px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	transition: all 0.1s ease;
}

header h3 {
	margin-top: 0;
}

main {
	margin: 20px 0 0;
}

.modal-enter-from {
	opacity: 0;
}

.modal-leave-to {
	opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
	-webkit-transform: scale(0.9);
	transform: scale(0.9);
}
</style>
