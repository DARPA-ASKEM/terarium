<template>
	<Button label="Open" @click="emit('open-drilldown')" outlined class="w-full" />
</template>

<script lang="ts" setup>
import Button from 'primevue/button';

const emit = defineEmits(['open-drilldown']);
</script>
