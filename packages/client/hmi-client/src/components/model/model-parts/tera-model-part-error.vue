<template>
	<section v-if="hasErrors" class="error">
		<Button size="small" severity="danger" @click.stop="filterByType('error')">Show errors</Button>
		<ul>
			<li v-for="item in errors" :key="item.id" @click.stop="filterByItem(item.id)">
				{{ item.content }}
			</li>
		</ul>
	</section>
	<section v-if="hasWarnings" class="warning">
		<Button size="small" severity="warning" @click.stop="filterByType('warn')">Show warnings</Button>
		<ul>
			<li v-for="item in warnings" :key="item.id" @click.stop="filterByItem(item.id)">
				{{ item.content }}
			</li>
		</ul>
	</section>
</template>

<script setup lang="ts">
import { ModelError } from '@/model-representation/service';
import { computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	items: ModelError[];
}>();

const emit = defineEmits(['filter-item', 'filter-type']);

const warnings = computed(() => props.items.filter((item) => item.severity === 'warn'));
const errors = computed(() => props.items.filter((item) => item.severity === 'error'));

const hasWarnings = computed(() => warnings.value.length > 0);
const hasErrors = computed(() => errors.value.length > 0);

const filterByItem = (id: string) => emit('filter-item', id);
const filterByType = (type: string) => emit('filter-type', type);
</script>

<style scoped>
section {
	border-radius: var(--border-radius);
	font-size: var(--font-body-small);
	font-weight: normal;
	margin-bottom: var(--gap-2);
	padding: var(--gap-2);
	padding-left: var(--gap-3);

	& > .p-button {
		float: right;
	}

	& + & {
		margin-top: var(--gap-2);
	}
}

ul {
	list-style-type: disc;
	padding: 0;

	li + li {
		margin-top: var(--gap-1);
	}

	li:hover {
		cursor: pointer;
		text-decoration: underline;
	}

	span {
		font-weight: var(--font-weight-semibold);

		&::after {
			content: ': ';
		}
	}
}

.warning {
	background-color: var(--surface-warning);
}

.error {
	background-color: var(--surface-error);
}
</style>
