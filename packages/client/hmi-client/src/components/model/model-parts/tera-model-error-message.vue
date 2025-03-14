<template>
	<aside>
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
	</aside>
</template>

<script setup lang="ts">
import { ModelError, ModelErrorSeverity } from '@/model-representation/service';
import { computed } from 'vue';
import Message from 'primevue/message';

const props = defineProps<{
	modelErrors: ModelError[];
}>();

const emit = defineEmits<{
	(e: 'filter-item', id: ModelError['id']): void;
}>();

const warnings = computed(() => props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.WARNING));
const errors = computed(() => props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.ERROR));

const hasWarnings = computed(() => warnings.value.length > 0);
const hasErrors = computed(() => errors.value.length > 0);

const filterByItem = (id: ModelError['id']) => emit('filter-item', id);
</script>

<style scoped>
ul {
	list-style-type: none;
	padding: 0;

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
