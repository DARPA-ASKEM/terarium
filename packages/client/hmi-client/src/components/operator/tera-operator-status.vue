<template>
	<section>
		<div class="icon-container" :class="`${status}`">
			<!--TODO: The progress spinner may later need to be specified to show how much is loaded so far-->
			<tera-progress-spinner v-if="notification.icon === 'spinner'" :font-size="3" />
			<vue-feather v-else :type="notification.icon" size="1.5rem" />
		</div>
		<slot />
		<p v-if="!hasSlot('default')">
			{{ notification.message }}
		</p>
	</section>
</template>

<script setup lang="ts">
import { PropType, useSlots } from 'vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { OperatorStatus } from '@/types/workflow';

const props = defineProps({
	status: {
		type: String as PropType<OperatorStatus>,
		default: OperatorStatus.DEFAULT
	}
});

const slots = useSlots();
const hasSlot = (name: string) => !!slots[name];

const notifications = {
	[OperatorStatus.SUCCESS]: { icon: 'check', message: 'Success' },
	[OperatorStatus.IN_PROGRESS]: { icon: 'spinner', message: 'Processing' },
	[OperatorStatus.ERROR]: { icon: 'alert-octagon', message: 'Error' }
};

const notification = notifications[props.status] ?? { icon: 'circle', message: '' };
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
	text-align: center;
	margin: 0.5rem 0;
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

.error,
.failed {
	background-color: var(--surface-error);
	& > i {
		color: var(--error-color);
	}
}
</style>
