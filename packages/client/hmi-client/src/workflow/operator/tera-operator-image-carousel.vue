<template>
	<Galleria v-if="!isEmpty(images)" :value="images" :showThumbnails="false" :showIndicators="true">
		<template #item="slotProps">
			<img :src="slotProps.item" alt="Alt" />
		</template>
	</Galleria>

	<div v-else class="no-image">
		<i class="pi pi-image" />
	</div>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { PropType } from 'vue';
import Galleria from 'primevue/galleria';

defineProps({
	images: {
		type: Array as PropType<string[]>, // image-hrefs and base64 formats work
		default: () => []
	}
});
</script>

<style scoped>
.p-galleria:deep(.p-galleria-indicators .p-galleria-indicator button) {
	width: 0.5rem;
	height: 0.5rem;
}

.p-galleria:deep(.p-galleria-item-container),
.p-galleria:deep(.p-galleria-item-container > div),
.no-image {
	height: 7.5rem;
	max-height: 7.5rem;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	overflow: hidden;
	background-color: var(--surface-ground);
}

i {
	margin: auto;
	color: rgb(226, 227, 229);
	font-size: 3rem;
}

.p-galleria:deep(img) {
	background-color: var(--surface-section);
	max-height: 7.5rem;
}
</style>
