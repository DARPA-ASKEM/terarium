<template>
	<div class="notification-button">
		<Button icon="pi pi-bell" severity="secondary" rounded text aria-label="Notifications" @click="togglePanel" />
		<span class="badge" v-if="unacknowledgedFinishedItems.length > 0"> {{ unacknowledgedFinishedItems.length }}</span>
	</div>
	<OverlayPanel class="notification-panel-container" ref="panel" @hide="acknowledgeFinishedItems">
		<header>
			<h1>Notifications</h1>
			<Button label="Clear notifications" text :disabled="!hasFinishedItems" @click="clearFinishedItems" />
		</header>
		<ul class="notification-items-container" v-if="sortedNotificationItems.length > 0">
			<li class="notification-item" v-for="item in sortedNotificationItems" :key="item.notificationGroupId">
				<p class="heading">
					{{ getTitleText(item) }}
					<tera-asset-link
						:label="item.sourceName"
						:asset-route="getAssetRoute(item)"
						:route-query="getAssetRouteQuery(item)"
					/>
				</p>
				<div class="notification-path-text msg">
					<p>
						<span v-if="item.context">{{ item.context }}/</span>{{ getProjectName(item) }}
					</p>
					<p v-if="item.status !== ProgressState.Complete">{{ item.msg }}</p>
				</div>
				<div v-if="showProgress" class="progressbar-container">
					<p class="action">
						{{ getActionText(item) }}
						<span v-if="item.progress && item.status === ProgressState.Running">
							{{ Math.round(item.progress * 100) }}%</span
						>
					</p>

					<ProgressBar
						v-if="item.progress !== undefined"
						:value="item.status === ProgressState.Running ? item.progress * 100 : 0"
					/>
					<ProgressBar v-else mode="indeterminate" />
					<Button
						v-if="item.supportCancel"
						class="cancel-button"
						label="Cancel"
						text
						aria-label="Cancel"
						@click="cancelTask(item)"
					/>
				</div>
				<div v-else class="done-container">
					<div class="status-msg ok" v-if="item.status === ProgressState.Complete">
						<i class="pi pi-check-circle" />Completed
					</div>
					<div class="status-msg cancel" v-if="item.status === ProgressState.Cancelled">
						<i class="pi pi-exclamation-circle" />Cancelled
					</div>
					<div class="status-msg error" v-else-if="isFailed(item)">
						<i class="pi pi-exclamation-circle" /> Failed: {{ item.error }}
					</div>
					<span class="time-msg">{{ getElapsedTimeText(item.lastUpdated) }}</span>
				</div>
			</li>
		</ul>
		<div v-else>
			<p class="text-body">There are no notifications to display.</p>
		</div>
	</OverlayPanel>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import OverlayPanel from 'primevue/overlaypanel';
import { NotificationItem } from '@/types/common';
import { AssetType, ClientEventType, ProgressState } from '@/types/Types';
import ProgressBar from 'primevue/progressbar';
import { computed, ref } from 'vue';
import { useNotificationManager } from '@/composables/notificationManager';
import { useProjects } from '@/composables/project';
import { getElapsedTimeText } from '@/utils/date';
import { cancelTask as cancelGoLLMTask } from '@/services/goLLM';
import { cancelCiemssJob } from '@/services/models/simulation-service';
import { orderBy } from 'lodash';
import TeraAssetLink from '../widgets/tera-asset-link.vue';

const {
	notificationItems,
	clearFinishedItems,
	acknowledgeFinishedItems,
	hasFinishedItems,
	unacknowledgedFinishedItems
} = useNotificationManager();

const panel = ref();

const togglePanel = (event) => panel.value.toggle(event);

const getTitleText = (item: NotificationItem) => `${item.typeDisplayName} from`;

const sortedNotificationItems = computed(() => orderBy(notificationItems.value, (item) => item.lastUpdated, ['desc']));

const showProgress = (item: NotificationItem) =>
	item.status === ProgressState.Running ||
	item.status === ProgressState.Cancelling ||
	item.status === ProgressState.Queued;
const isFailed = (item: NotificationItem) =>
	item.status === ProgressState.Failed || item.status === ProgressState.Error;

const getProjectName = (item: NotificationItem) =>
	(useProjects().allProjects.value || []).find((p) => p.id === item.projectId)?.name || '';

const getActionText = (item: NotificationItem) => {
	if (item.status === ProgressState.Cancelling) {
		return 'Cancelling...';
	}
	if (item.status === ProgressState.Queued) {
		return 'Queued...';
	}
	switch (item.type) {
		case ClientEventType.ExtractionPdf:
			return 'Extracting...';
		default:
			return 'Processing...';
	}
};

const getAssetRoute = (item: NotificationItem) => ({
	assetId: item.assetId as string,
	projectId: item.projectId,
	pageType: item.pageType
});
const getAssetRouteQuery = (item: NotificationItem) =>
	item.pageType === AssetType.Workflow && item.nodeId ? { operator: item.nodeId } : {};

const cancelTask = (item: NotificationItem) => {
	if (!item.supportCancel) return;
	if (
		[
			ClientEventType.TaskGollmConfigureModelFromDocument,
			ClientEventType.TaskGollmConfigureModelFromDataset,
			ClientEventType.TaskGollmCompareModel
		].includes(item.type)
	) {
		cancelGoLLMTask(item.notificationGroupId);
	}
	if (item.type === ClientEventType.SimulationNotification) {
		item.status = ProgressState.Cancelling;
		cancelCiemssJob(item.notificationGroupId);
	}
};
</script>

<style>
/*
 * Reset the default overlay component container style.
 * Note that this style block isn't scoped since the overlay component is appended to the body html dynamically when opened
 * and is placed outside of this component's scope and the scoped styles aren't applied to it.
 */
.notification-panel-container.p-overlaypanel {
	top: var(--navbar-outer-height) !important;
	width: 34rem;
	box-shadow: 0 4px 4px 0 #00000040;
	padding: var(--content-padding);
	padding-bottom: 1.5rem;
	gap: var(--gap-4);
	border: 1px solid var(--surface-border-alt);
	border-radius: var(--border-radius-medium);
	.p-overlaypanel-content {
		padding: 0;
	}
	&:after,
	&:before {
		content: none;
	}
}
</style>

<style scoped>
.notification-button {
	position: relative;
	.p-button.p-button-secondary.p-button-text {
		color: var(--text-color-subdued);
	}
	.p-button.p-button-secondary.p-button-text:enabled:focus,
	.p-button.p-button-secondary.p-button-text:enabled:hover {
		background-color: #e3efe4;
	}
	.badge {
		position: absolute;
		background-color: var(--error-color);
		color: #fff;
		top: -5px;
		right: -5px;
		font-size: var(--font-tiny);
		border-radius: 10px;
		padding: 2px 5px;
	}
}
header {
	display: flex;
	justify-content: space-between;
	align-items: center;

	h1 {
		font-size: var(--font-body-medium);
		font-weight: var(--font-weight-semibold);
	}
	button {
		padding: 1px;
		font-size: var(--font-caption);
	}
}
.text-body {
	font-size: var(--font-body-small);
	color: var(--text-color-secondary);
	margin-top: 1rem;
}

.notification-items-container {
	max-height: 570px;
	list-style: none;
	overflow: auto;
}

.notification-path-text {
	padding-top: 3px;
}

.cancel-button {
	font-size: var(--font-caption);
}

.notification-item {
	padding: 1rem 0;
	&:not(:first-child) {
		border-top: 1px solid #dee2e6;
	}
	.heading {
		font-size: var(--font-body-small);
	}
	.heading a {
		color: var(--primary-color);
	}
	.msg {
		font-size: var(--font-caption);
		color: var(--text-color-secondary);
		margin-top: 0.2rem;
		/* color: #9298a5; */
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
		gap: var(--gap-2);
	}
	.p-progressbar {
		flex-grow: 1;
	}

	.done-container {
		margin-top: 0.5rem;
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: var(--gap-2);
		.status-msg {
			display: flex;
			gap: 0.5rem;
			font-size: var(--font-caption);
		}
		.status-msg.ok {
			color: var(--primary-color);
		}
		.status-msg.error,
		.status-msg.cancel {
			color: var(--error-color);
		}
		.time-msg {
			font-size: var(--font-caption);
			color: var(--text-color-secondary);
			min-width: 96px;
			text-align: right;
		}
	}
}
</style>
