<template>
	<full-screen-modal-header :showIcon="true" :nav-back-label="navBackLabel" @close="onBack">
		<slot name="content"></slot>
	</full-screen-modal-header>
</template>

<script setup lang="ts">
import FullScreenModalHeader from '@/components/widgets/full-screen-modal-header.vue';

defineProps({
	navBackLabel: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['close']);

const onBack = () => {
	emit('close');
};
</script>
