<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
	isVisible: Boolean
});
</script>

<template>
	<Transition name="modal">
		<div v-if="isVisible" class="modal-mask">
			<div class="modal-wrapper">
				<div class="modal-container">
					<div class="modal-header">
						<slot name="header"></slot>
					</div>

					<div class="modal-body">
						<slot name="body"></slot>
					</div>

					<div class="modal-footer">
						<slot name="footer"> </slot>
					</div>
				</div>
			</div>
		</div>
	</Transition>
</template>

<style>
.modal-mask {
	position: fixed;
	z-index: 9998;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: table;
	transition: opacity 0.1s ease;
}

.modal-wrapper {
	display: table-cell;
	vertical-align: middle;
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

.modal-header h3 {
	margin-top: 0;
}

.modal-body {
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
