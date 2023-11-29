<template>
	<Galleria
		v-if="!isEmpty(images)"
		:value="images"
		:numVisible="5"
		containerStyle="max-width: 640px"
		:showThumbnails="false"
		:showIndicators="true"
	>
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
		type: Array as PropType<string[]>,
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
.no-image {
	height: 240px;
	max-height: 240px;
	border: 1px solid #e7e9ee;
	border-radius: 0.5rem;
	background-color: var(--surface-ground);
}

i {
	margin: auto;
	color: rgb(226, 227, 229);
	font-size: 3rem;
}

.p-galleria:deep(img) {
	background-color: var(--surface-section);
}
</style>
