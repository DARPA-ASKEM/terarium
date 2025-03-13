<template>
	<Message v-if="hasErrors" severity="error">
		<ul>
			<li v-for="item in errors" :key="item.id" @click.stop="filterByItem(item.id)">
				{{ item.content }}
			</li>
		</ul>
	</Message>
	<Message v-if="hasWarnings" severity="warn">
		<ul>
			<li v-for="item in warnings" :key="item.id" @click.stop="filterByItem(item.id)">
				{{ item.content }}
			</li>
		</ul>
	</Message>
</template>

<script setup lang="ts">
import { ModelError, ModelErrorSeverity } from '@/model-representation/service';
import { computed } from 'vue';
import Message from 'primevue/message';

const props = defineProps<{
	items: ModelError[];
}>();

const emit = defineEmits(['filter-item']);

const warnings = computed(() => props.items.filter((item) => item.severity === ModelErrorSeverity.WARNING));
const errors = computed(() => props.items.filter((item) => item.severity === ModelErrorSeverity.ERROR));

const hasWarnings = computed(() => warnings.value.length > 0);
const hasErrors = computed(() => errors.value.length > 0);

const filterByItem = (id: string) => emit('filter-item', id);
</script>

<style scoped>
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
</style>
