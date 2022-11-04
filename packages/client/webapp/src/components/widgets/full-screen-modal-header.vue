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

<script lang="ts">
import { defineComponent } from 'vue';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';

export default defineComponent({
	name: 'FullScreenModalHeader',
	components: {
		IconChevronLeft32
	},
	props: {
		showIcon: {
			type: Boolean,
			default: false
		},
		navBackLabel: {
			type: String,
			default: ''
		}
	},
	emits: ['close'],
	methods: {
		close() {
			this.$emit('close');
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';

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
	font-weight: 600;
	font-size: var(--font-size-large);
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
