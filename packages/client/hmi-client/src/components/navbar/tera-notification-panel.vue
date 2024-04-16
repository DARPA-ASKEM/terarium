<template>
	<div class="notification-button">
		<Button
			icon="pi pi-bell"
			severity="secondary"
			rounded
			text
			aria-label="Notifications"
			@click="togglePanel"
		/>
		<span class="badge" v-if="unacknowledgedFinishedItems.length > 0">
			{{ unacknowledgedFinishedItems.length }}</span
		>
	</div>
	<OverlayPanel class="notification-container" ref="panel" @hide="acknowledgeFinishedItems">
		<div class="header">
			<h1>Notifications</h1>
			<Button
				label="Clear notifications"
				text
				:disabled="!hasFinishedItems"
				@click="clearFinishedItems"
			/>
		</div>
		<ul class="notification-items-container" v-if="notificationItems.length > 0">
			<li class="notification-item" v-for="item in notificationItems" :key="item.id">
				<p class="heading">
					{{ getTitleText(item) }}
					<tera-asset-link text-only :label="item.assetName" :asset-route="getAssetRoute(item)" />
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
						<i class="pi pi-exclamation-circle" /> Failed: {{ item.error }}
					</div>
					<span class="time-msg">{{ getElapsedTimeText(item) }}</span>
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
import { AssetType, ClientEventType } from '@/types/Types';
import ProgressBar from 'primevue/progressbar';
import { ref } from 'vue';
import { useNotificationManager } from '@/composables/notificationManager';
import TeraAssetLink from '../widgets/tera-asset-link.vue';

const {
	itemsForActiveProject: notificationItems,
	clearFinishedItems,
	acknowledgeFinishedItems,
	hasFinishedItems,
	unacknowledgedFinishedItems
} = useNotificationManager();

const panel = ref();

const togglePanel = (event) => panel.value.toggle(event);

const getTitleText = (item: NotificationItem) => {
	switch (item.type) {
		case ClientEventType.ExtractionPdf:
			return 'PDF extraction from';
		default:
			return 'Process';
	}
};

const getActionText = (item: NotificationItem) => {
	switch (item.type) {
		case ClientEventType.ExtractionPdf:
			return 'Extracting...';
		default:
			return 'Processing...';
	}
};

const getAssetRoute = (item: NotificationItem) => {
	switch (item.type) {
		case ClientEventType.ExtractionPdf:
			return { assetId: item.id, pageType: AssetType.Document };
		default:
			return { assetId: item.id, pageType: AssetType.Document };
	}
};

const getElapsedTimeText = (item: NotificationItem) => {
	const time = Date.now() - item.lastUpdated;
	const minutes = Math.floor(time / (1000 * 60));
	return minutes > 0 ? `${minutes} minutes ago` : 'Just now';
};
</script>

<style>
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
/* Reset default overlay component style */
.notification-container.p-overlaypanel .p-overlaypanel-content {
	padding: 0;
}
.notification-container.p-overlaypanel:after,
.notification-container.p-overlaypanel:before {
	content: none;
}
.notification-container.p-overlaypanel {
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

.notification-items-container {
	max-height: 570px;
	list-style: none;
	overflow: auto;
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
