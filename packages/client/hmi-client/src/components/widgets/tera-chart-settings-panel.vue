<template>
	<transition>
		<div class="chart-settings-panel" v-if="isOpen">
			<header :class="{ shadow: false }">
				<Button :icon="`pi pi-times`" @click="isOpen = false" text rounded size="large" />
				<h4>Chart Settings</h4>
			</header>
			<div class="content">
				<h5>Annotations</h5>
			</div>
		</div>
	</transition>
</template>

<script setup lang="ts">
// import { emit } from 'vue';
import Button from 'primevue/button';

const isOpen = defineModel({ required: true, default: false });

defineProps({
	settings: {
		type: Object,
		default: () => ({})
	},
	annotations: {
		type: Array,
		default: () => []
	}
});
defineEmits(['close', 'update:settings']);
</script>

<style scoped>
.chart-settings-panel {
	position: absolute;
	top: 0;
	z-index: 3;
	height: 100%;
	width: 100%;
	background: #fff;
	left: 0;

	&.v-enter-active,
	&.v-leave-active {
		transition: left 0.15s ease-in;
		left: 30%;
	}
	&.v-enter-from,
	&.v-leave-to {
		left: 100%;
	}

	header {
		position: sticky;
		top: 0;
		z-index: 3;
		display: flex;
		align-items: center;
		flex-direction: row-reverse;
		justify-content: space-between;
		padding: var(--gap-2);
		padding-left: var(--gap);
		gap: var(--gap);
		background-color: rgba(255, 255, 255, 0.8);
		backdrop-filter: blur(3px);
		&.shadow {
			box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.1);
		}
		button {
			height: 2.5rem;
		}
	}
	.content {
		padding: 1rem;
	}
}
</style>
