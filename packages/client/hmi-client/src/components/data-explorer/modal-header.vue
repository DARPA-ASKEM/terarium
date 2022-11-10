<template>
	<full-screen-modal-header :showIcon="true" :nav-back-label="navBackLabel" @close="onBack">
		<slot name="content"></slot>
	</full-screen-modal-header>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FullScreenModalHeader from '@/components/widgets/full-screen-modal-header.vue';

export default defineComponent({
	name: 'DataExplorerModalHeader',
	components: {
		FullScreenModalHeader
	},
	props: {
		navBackLabel: {
			type: String,
			default: ''
		}
	},
	emits: ['close'],
	methods: {
		onBack() {
			this.$emit('close');
		}
	}
});
</script>
