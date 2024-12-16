<template>
	<div>
		<label v-if="label">{{ label }}</label>
		<Dropdown :model-value="modelValue" :options="options" @update:model-value="updateValue">
			<template #value="{ value }">
				{{ value.toString() }}
			</template>
		</Dropdown>
		<!-- Signal Bars -->
		<ul v-if="minOption > 0">
			<li v-for="n in options.length" :key="n" :class="{ active: n <= modelValue }" :style="getBarStyle(n)" />
		</ul>
		<ul v-if="minOption <= 0">
			<li v-for="n in options.length - 1" :key="n" :class="{ active: n <= modelValue }" :style="getBarStyle(n)" />
		</ul>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import Dropdown from 'primevue/dropdown';
import { ref } from 'vue';

const baseDelay = 0.03; // transition delay for each bar
const barWidth = 2; // width for each bar

const props = defineProps({
	modelValue: {
		type: Number,
		required: true
	},
	minOption: {
		type: Number,
		default: 0
	},
	maxOption: {
		type: Number,
		default: 10
	},
	label: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['update:modelValue']);

const previousValue = ref(props.modelValue);

const options: number[] = _.range(props.minOption, props.maxOption + 1, 1);

// fun styling for the bars :)
const getBarStyle = (n: number) => {
	const isIncreasing = props.modelValue > previousValue.value;
	const delay = isIncreasing
		? (n - Math.min(props.modelValue, previousValue.value)) * baseDelay
		: (Math.max(props.modelValue, previousValue.value) - n) * baseDelay;

	// might be worth to make the height based on the nuyber of bars
	return {
		height: `${n * barWidth}px`,
		transitionDelay: `${delay}s`
	};
};

const updateValue = (value: number) => {
	previousValue.value = props.modelValue;
	emit('update:modelValue', value);
};
</script>

<style scoped>
div {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
}

ul {
	display: flex;
	align-items: flex-end;
	gap: var(--gap-0-5);
	list-style: none;
}

li {
	width: 4px;
	background-color: var(--surface-border);
	border-radius: var(--border-radius);
	transition:
		height 0.5s ease,
		background-color 0.5s ease;
	&.active {
		background-color: var(--primary-color);
	}
}

/* static width for dropdown */
:deep(.p-dropdown) {
	width: 5rem;
}
</style>
