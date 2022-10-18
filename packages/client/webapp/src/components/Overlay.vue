<template>
	<div class="overlay-container">
		<div class="message-container"><i class="fa fa-spin fa-spinner" /> {{ message }}</div>
		<div v-if="messageSecondary !== ''" class="message-secondary-container">
			{{ messageSecondary }}
		</div>
		<div v-if="cancelFn" style="font-size: 2rem">
			<button type="button" class="btn" @click="cancelFn">Cancel</button>
		</div>
	</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
/**
 * Simple overlay for loading things
 */
export default defineComponent({
	name: 'Overlay',
	props: {
		message: {
			type: String,
			default: 'Loading...'
		},
		messageSecondary: {
			type: String,
			default: ''
		},
		cancelFn: {
			type: Function as PropType<Function | null>,
			default: null
		}
	}
});
</script>

<style scoped>
.overlay-container {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	background: #000;
	opacity: 0.7;
	z-index: 9999;
}

.message-container {
	font-size: 4rem;
	color: var(--un-color-black-20);
}

.message-secondary-container {
	font-size: 2.25rem;
	color: var(--un-color-black-20);
}
</style>
