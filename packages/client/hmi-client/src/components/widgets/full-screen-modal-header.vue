<template>
	<div class="full-screen-modal-header-container">
		<div class="navBack" @click="close">
			<IconChevronLeft32 v-if="showIcon" />
			{{ navBackLabel }}
		</div>
		<div class="centered-slot">
			<slot />
		</div>
		<slot name="trailing" />
	</div>
</template>

<script setup lang="ts">
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';

defineProps({
	showIcon: {
		type: Boolean,
		default: false
	},
	navBackLabel: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['close']);

const close = () => {
	emit('close');
};
</script>

<style scoped>
.full-screen-modal-header-container {
	background-color: var(--un-color-accent-dark);
	display: flex;
	align-items: center;
	min-height: 48px;
	position: relative;
}

.centered-slot {
	flex-grow: 1;
	display: flex;
	justify-content: center;
	align-items: center;
	color: #ffffff;
	margin: 0 15px;
}

.navBack {
	display: inline-flex;
	align-items: center;
	color: #ffffff;
	cursor: pointer;
	margin: 0 15px;
}

.navBack span {
	box-sizing: border-box;
}

.navBack:hover {
	text-decoration: underline;
}
</style>
