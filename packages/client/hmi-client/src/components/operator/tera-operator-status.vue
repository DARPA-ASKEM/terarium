<template>
	<section>
		<div
			v-if="props.status && props.status !== OperatorStatus.DEFAULT && props.status !== OperatorStatus.SUCCESS"
			class="icon-container"
			:class="`${status}`"
		>
			<div
				v-if="props.status === OperatorStatus.IN_PROGRESS || props.status === ProgressState.Running"
				class="progressbar-container"
			>
				<div v-if="props.progress">
					<p class="action">{{ Math.round(props.progress * 100) }}%</p>
					<ProgressBar :value="props.progress ? props.progress * 100 : 0" />
				</div>
				<tera-progress-spinner v-else :font-size="2" />
			</div>
			<vue-feather v-else :type="notificationStatusMap[props.status].icon" size="1.5rem" />
		</div>
	</section>
	<div v-if="!hasSlot('default')" class="action">
		{{ notificationStatusMap[props.status].message }}
	</div>
	<slot v-else />
</template>

<script setup lang="ts">
import { useSlots } from 'vue';
import ProgressBar from 'primevue/progressbar';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import { ProgressState } from '@/types/Types';
import { OperatorStatus } from '@/types/workflow';

const props = defineProps<{
	status: OperatorStatus | ProgressState;
	progress?: number;
}>();

const slots = useSlots();
const hasSlot = (name: string) => !!slots[name];
const notificationStatusMap = {
	[OperatorStatus.SUCCESS]: { icon: 'check', message: 'Success' },
	[OperatorStatus.IN_PROGRESS]: { icon: 'spinner', message: 'Processing' },
	[OperatorStatus.ERROR]: { icon: 'alert-octagon', message: 'Error' },
	[OperatorStatus.INVALID]: { icon: 'alert-octagon', message: 'Invalid' },
	[OperatorStatus.DEFAULT]: { icon: 'circle', message: '' }
};
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
.progressbar-container {
	margin-top: var(--gap-2);
	display: flex;
	justify-content: space-between;
	gap: var(--gap-2);
}

.p-progressbar {
	flex-grow: 1;
}

.action {
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	text-align: center;
}
</style>
