<template>
	<Button icon="pi pi-check" rounded aria-label="Process Manager" @click="togglePanel" />
	<OverlayPanel class="process-manager-container" ref="panel">
		<div class="header">
			<h1>Process Manager</h1>
			<Button
				label="Clear notifications"
				text
				:disabled="!hasFinishedItems"
				@click="clearFinishedItems"
			/>
		</div>
		<ul class="process-items-container" v-if="processItems.length > 0">
			<li class="process-item" v-for="item in processItems" :key="item.id">
				<p class="heading">
					{{ getHeadingText(item) }} <span>{{ item.assetName }}</span>
				</p>
				<p class="msg">{{ item.msg }}</p>
				<div v-if="item.status === 'Running'" class="progressbar-container">
					<p class="action">{{ getActionText(item) }}</p>
					<ProgressBar :value="item.progress * 100" />
				</div>
				<div v-else class="done-container">
					<div class="status-msg ok" v-if="item.status === 'Completed'">
						<i class="pi pi-check-circle" />Completed
					</div>
					<div class="status-msg error" v-else-if="item.status === 'Failed'">
						<i class="pi pi-exclamation-circle" /> Failed: {{ item.msg }}
					</div>
					<span class="time-msg">{{ getElapsedTimeText(item) }}</span>
				</div>
			</li>
		</ul>
		<div v-else>
			<p class="text-body">There are no process notifications to display.</p>
		</div>
	</OverlayPanel>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import OverlayPanel from 'primevue/overlaypanel';
import { ProcessItem } from '@/types/common';
import { useProcessManager } from '@/composables/processManager';
import { ClientEventType } from '@/types/Types';
import ProgressBar from 'primevue/progressbar';
import { ref, computed, watch } from 'vue';
import { useProjects } from '@/composables/project';

const { activeProjectId } = useProjects();
const {
	itemsByActiveProject: processItems,
	clearFinishedItems,
	setActiveProjectId
} = useProcessManager();

watch(activeProjectId, (val) => setActiveProjectId(val));

const panel = ref();
const hasFinishedItems = computed(() =>
	processItems.value.some(
		(item: ProcessItem) => item.status === 'Completed' || item.status === 'Failed'
	)
);

const togglePanel = (event) => {
	panel.value.toggle(event);
};

const getHeadingText = (item: ProcessItem) => {
	switch (item.type) {
		case ClientEventType.Extraction:
			return 'PDF extraction from';
		default:
			return 'Process';
	}
};

const getActionText = (item: ProcessItem) => {
	switch (item.type) {
		case ClientEventType.Extraction:
			return 'Extracting...';
		default:
			return 'Processing...';
	}
};

const getElapsedTimeText = (item: ProcessItem) => {
	const time = Date.now() - item.lastUpdated;
	const minutes = Math.floor(time / (1000 * 60));
	return minutes > 0 ? `${minutes} minutes ago` : 'Just now';
};
</script>

<style>
/* Reset default overlay component style */
.process-manager-container.p-overlaypanel .p-overlaypanel-content {
	padding: 0;
}
.process-manager-container.p-overlaypanel:after,
.process-manager-container.p-overlaypanel:before {
	content: none;
}
.process-manager-container.p-overlaypanel {
	top: 51px !important;
	width: 540px;
	box-shadow: 0px 4px 4px 0px #00000040;
	padding: var(--content-padding);
	padding-bottom: 1.5rem;
	gap: var(--gap);
	border: 1px solid #c3ccd6;
	border-radius: var(--border-radius-medium);
}
.header {
	display: flex;
	justify-content: space-between;
	align-items: center;

	h1 {
		font-size: var(--font-body-medium);
		font-weight: var(--font-weight-semibold);
	}
	button {
		padding: 1px;
	}
}
.text-body {
	font-size: var(--font-body-small);
	color: var(--text-color-secondary);
}

.process-items-container {
	max-height: 570px;
	list-style: none;
	overflow: auto;
}

.process-item {
	padding: 1rem 0;
	&:not(:first-child) {
		border-top: 1px solid #dee2e6;
	}
	.heading {
		font-size: var(--font-body-small);
	}
	.heading span {
		color: var(--primary-color);
	}
	.msg {
		font-size: var(--font-caption);
		color: #9298a5;
	}
	.action {
		font-size: var(--font-caption);
		color: var(--text-color-secondary);
	}
	.progressbar-container {
		margin-top: 0.5rem;
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: var(--gap-small);
	}
	.p-progressbar {
		flex-grow: 1;
	}

	.done-container {
		margin-top: 0.5rem;
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: var(--gap-small);
	}
	.done-container .status-msg {
		display: flex;
		align-items: center;
		gap: 0.5rem;
	}
	.done-container .status-msg.ok {
		color: var(--primary-color);
	}
	.done-container .status-msg.error {
		color: var(--error-color);
	}
	.done-container .time-msg {
		font-size: var(--font-caption);
		color: var(--text-color-secondary);
	}
}
</style>
