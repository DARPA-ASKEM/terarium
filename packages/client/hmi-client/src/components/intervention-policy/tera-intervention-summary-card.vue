<template>
	<div>
		<aside>{{ !isEmpty(intervention.dynamicInterventions) ? 'Dynamic' : 'Static' }}</aside>
		<h5>{{ intervention.name }}</h5>
		<ul>
			<li v-for="(staticIntervention, index) in intervention.staticInterventions" :key="`static-${index}`">
				Set {{ staticIntervention.type }} <strong>{{ staticIntervention.appliedTo }}</strong> to
				<strong>{{ staticIntervention.value }}</strong> at start time <strong>{{ staticIntervention.timestep }}</strong
				>&nbsp;day.
			</li>
			<li v-for="(dynamicIntervention, index) in intervention.dynamicInterventions" :key="`dynamic-${index}`">
				Set {{ dynamicIntervention.type }} <strong>{{ dynamicIntervention.appliedTo }}</strong> to
				<strong>{{ dynamicIntervention.value }}</strong> when
				<strong>{{ dynamicIntervention.parameter }}</strong> crosses the threshold&nbsp;<strong>{{
					dynamicIntervention.threshold
				}}</strong
				>.
			</li>
		</ul>
	</div>
</template>

<script setup lang="ts">
import { Intervention } from '@/types/Types';
import { isEmpty } from 'lodash';

defineProps<{
	intervention: Intervention;
}>();
</script>

<style scoped>
div {
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

li + li {
	margin-top: var(--gap-1);
}

aside {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	float: right;
}
</style>
