<template>
	<div class="full-screen-modal-header-container">
		<div class="navBack" @click="close">
			<i v-if="icon !== null" class="fa fa-fw" :class="`fa-${icon}`" />
			<span>{{ navBackLabel }}</span>
		</div>
		<div class="centered-slot">
			<slot />
		</div>
		<slot name="trailing" />
	</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
	name: 'FullScreenModalHeader',
	props: {
		icon: {
			type: String,
			default: null
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
@import '../../styles/variables.scss';

.full-screen-modal-header-container {
	background-color: $accent-dark;
	display: flex;
	align-items: center;
	min-height: 48px;
	position: relative;

	& > * {
		margin: 0 15px;

		&:first-child {
			margin-left: 10;
		}

		&:last-child {
			margin-right: 0;
		}
	}

	.centered-slot {
		flex-grow: 1;
		display: flex;
		justify-content: center;
		align-items: center;
		color: #ffffff;
	}

	.navBack {
		font-weight: 600;
		font-size: $font-size-large;
		color: #ffffff;
		cursor: pointer;
		i {
			margin-right: 5px;
		}
	}

	.navBack:hover {
		text-decoration: underline;
	}
}
</style>
