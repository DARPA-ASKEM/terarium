<template>
	<div class="container" :class="{ hide: hide || isEmpty(traceback) }">
		<h6>
			<span>{{ props.name }}</span>
			<Button rounded text icon="pi pi-times" @click="hide = true" />
		</h6>
		<p>{{ props.value }}</p>
		<code>{{ props.traceback }}</code>
	</div>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	name?: string;
	value?: string;
	traceback?: string;
}>();

const hide = ref(false);

watch(
	() => props.traceback,
	() => {
		hide.value = false;
	}
);
</script>

<style scoped>
.container {
	margin: 0 var(--gap-3) var(--gap-3) var(--gap-3);
	background-color: #ffdcdc;
	padding: var(--gap-3);
	border-radius: var(--border-radius);
}

h6 {
	display: flex;
	justify-content: space-between;
}

.hide {
	display: none;
}
</style>
