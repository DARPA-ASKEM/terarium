<template>
	<section>
		<h5>{{ intervention.name }}</h5>
		<ul>
			<li v-for="(staticIntervention, index) in intervention.staticInterventions" :key="`static-${index}`">
				Set the <strong>{{ staticIntervention.type }}</strong> <strong>{{ staticIntervention.appliedTo }}</strong> to
				the value of <strong>{{ staticIntervention.value }}</strong> day at start time
				<strong>{{ staticIntervention.timestep }}</strong> day.
			</li>
			<li v-for="(dynamicIntervention, index) in intervention.dynamicInterventions" :key="`dynamic-${index}`">
				Set the <strong>{{ dynamicIntervention.type }}</strong> <strong>{{ dynamicIntervention.appliedTo }}</strong> to
				<strong>{{ dynamicIntervention.threshold }}</strong> days when it crosses the threshold value
				<strong>{{ dynamicIntervention.value }}</strong> person.
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { Intervention } from '@/types/Types';

defineProps<{
	intervention: Intervention;
}>();
</script>

<style scoped>
section {
	background: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius-medium);
	padding: var(--gap-4) var(--gap-5);
	box-shadow:
		0 2px 2px -1px rgba(0, 0, 0, 0.06),
		0 2px 4px -1px rgba(0, 0, 0, 0.08);
}

ul {
	list-style: none;
	margin-top: var(--gap-2);
}
</style>
