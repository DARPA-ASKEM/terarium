<template>
	<section ref="tooltipRef" class="inside" :class="type ? 'show' : 'hide'">
		<Card v-if="type === DistributionType.Constant">
			<template #title>Constant</template>
			<template #content>
				Constants description
				<h5 class="pt-3 pb-2">Required parameters:</h5>
				<ul class="ml-5">
					<li class="pb-2">
						<h6>Value</h6>
						<p class="parameter-description">value description</p>
					</li>
				</ul>
			</template>
		</Card>
		<Card v-else-if="type === DistributionType.Uniform">
			<template #title
				>Uniform
				<a
					v-tooltip="'Uniform pytorch.org docs'"
					:href="`${externaDoclLink}#uniform`"
					:aria-label="'Uniform docs'"
					rel="noopener noreferrer"
					target="_blank"
				>
					<span class="pi pi-external-link"></span>
				</a>
			</template>
			<template #content>
				Generates uniformly distributed random samples from the half-open interval.
				<h5 class="pt-3 pb-2">Required parameters:</h5>
				<ul class="ml-5">
					<li class="pb-2">
						<h6>Min</h6>
						<p class="parameter-description">Lower bound of the interval</p>
					</li>
					<li class="pb-2">
						<h6>Max</h6>
						<p class="parameter-description">Upper bound of the interval</p>
					</li>
				</ul>
			</template>
		</Card>
	</section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import Card from 'primevue/card';
import { DistributionType, externaDoclLink } from '@/services/distribution';

const props = defineProps<{
	distributionType: string | null;
}>();

const tooltipRef = ref(null);
const type = computed(() => props.distributionType);

defineExpose({ tooltipRef });
</script>

<style scoped>
.hide {
	visibility: hidden;
	min-height: 0;
	min-width: 0;
}
.show {
	visibility: visible;
	min-height: 100px;
	min-width: 300px;
}
.inside {
	position: absolute;
}
.parameter-description {
	color: var(--text-color-subdued);
	font-style: italic;
}
</style>
