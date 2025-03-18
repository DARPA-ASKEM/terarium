<template>
	<aside>
		<Message v-if="hasErrors" severity="error">
			<ul>
				<li v-for="item in errors" :key="item.id">
					{{ item.content }}
				</li>
			</ul>
		</Message>
		<Message v-if="hasWarnings" severity="warn">
			<ul>
				<li v-for="item in warnings" :key="item.id">
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

const warnings = computed(() => props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.WARNING));
const errors = computed(() => props.modelErrors.filter(({ severity }) => severity === ModelErrorSeverity.ERROR));

const hasWarnings = computed(() => warnings.value.length > 0);
const hasErrors = computed(() => errors.value.length > 0);
</script>

<style scoped>
aside {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);

	& > div.p-message {
		margin: 0;

		&:last-of-type {
			margin-bottom: var(--gap-2);
		}
	}
}

ul {
	list-style-type: none;
	padding: 0;

	span {
		font-weight: var(--font-weight-semibold);

		&::after {
			content: ': ';
		}
	}
}
</style>
