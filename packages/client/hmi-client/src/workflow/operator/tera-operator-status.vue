<template>
	<section>
		<div class="icon-container" :class="`${status}`">
			<vue-feather :type="statusIcon[status]" size="1.5rem" />
		</div>
		<slot />
	</section>
</template>

<script setup lang="ts">
import { PropType } from 'vue';
import { OperatorStatus } from '@/types/workflow';

defineProps({
	status: {
		type: String as PropType<OperatorStatus>,
		default: OperatorStatus.DEFAULT
	}
});

const statusIcon = {
	[OperatorStatus.SUCCESS]: 'check',
	[OperatorStatus.WARNING]: 'alert-triangle',
	[OperatorStatus.ERROR]: 'alert-octagon'
};
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
	text-align: center;
}

.icon-container {
	width: 50px;
	height: 50px;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 0.5rem auto;
	border-radius: 10rem;
}

.success {
	background-color: var(--surface-highlight);
	& > i {
		color: var(--primary-color);
	}
}

.warning {
	background-color: var(--surface-warning);
	& > i {
		color: var(--warning-color);
	}
}

.error,
.failed {
	background-color: var(--surface-error);
	& > i {
		color: var(--error-color);
	}
}
</style>
