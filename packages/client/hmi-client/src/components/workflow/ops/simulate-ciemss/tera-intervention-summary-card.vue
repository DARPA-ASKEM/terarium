<template>
	<section class="policy-group">
		<h5>{{ intervention.name }}</h5>
		<ul>
			<li v-for="(staticIntervention, index) in intervention.staticInterventions" :key="index">
				Set the <strong>{{ intervention?.type }}</strong
				>&nbsp; <strong>{{ intervention?.appliedTo }}</strong> to the value of
				<strong>{{ staticIntervention.value }}</strong> day at start time
				<strong>{{ staticIntervention.timestep }}</strong> day.
			</li>
			<li v-for="(dynamicIntervention, index) in intervention.dynamicInterventions" :key="`dynamic_${index}`">
				Set the {{ intervention?.type }}&nbsp; <strong>{{ intervention?.appliedTo }}</strong> to
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
.policy-group {
	display: flex;
	padding: var(--gap-4);
	padding-left: var(--gap-5);
	flex-direction: column;
	justify-content: center;
	align-items: flex-start;
	gap: var(--gap-2);
	border-radius: var(--gap-1-5);
	background: var(--surface-section);
	border: 1px solid var(--surface-border-light);
	/* Shadow/medium */
	box-shadow:
		0 2px 4px -1px rgba(0, 0, 0, 0.06),
		0 4px 6px -1px rgba(0, 0, 0, 0.08);
}

ul {
	list-style: none;
}
</style>
