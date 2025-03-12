<template>
	<main>
		<section v-if="hasErrors" class="error">
			<ul>
				<li v-for="item in errors" :key="item.id" @click.stop="filterByItem(item.id)">
					<span>{{ item.id }}</span>
					{{ item.content }}
				</li>
			</ul>
			<Button severity="danger" @click.stop="filterByType('error')">Show errors</Button>
		</section>
		<section v-if="hasWarnings" class="warning">
			<ul>
				<li v-for="item in warnings" :key="item.id" @click.stop="filterByItem(item.id)">
					<span>{{ item.id }}</span>
					{{ item.content }}
				</li>
			</ul>
			<Button severity="warning" @click.stop="filterByType('warn')">Show warnings</Button>
		</section>
	</main>
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
const errors = computed(() => props.items.filter((item) => item.severity === 'warn'));

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
	padding: var(--gap-4);
}

strong {
	font-weight: var(--font-weight-semibold);
}

ul {
	list-style-type: none;
	padding: 0;

	li + li {
		margin-top: var(--gap-1);
	}

	li::before {
		content: '- ';
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

:deep(.p-button-text) {
	padding: 0;
}
</style>
