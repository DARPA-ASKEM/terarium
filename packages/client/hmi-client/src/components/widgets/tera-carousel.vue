<template>
	<figure>
		<div ref="content" class="content">
			<slot />
		</div>
		<nav v-if="itemCount > 0">
			<ul v-if="isNumeric" class="numeric">
				<li
					v-for="(_, index) in itemCount"
					:class="{ selected: index === currentPage }"
					:key="_"
					@click.stop="move(index)"
				>
					{{ index + 1 }}
				</li>
				<li v-if="itemCount > 5">(+{{ itemCount }})</li>
			</ul>
			<ul v-else>
				<li
					v-for="(_, index) in itemCount"
					:class="{ selected: index === currentPage }"
					:key="_"
					@click.stop="move(index)"
				/>
			</ul>
		</nav>
	</figure>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

defineProps({
	isNumeric: {
		type: Boolean,
		default: false
	}
});

const content = ref();
const currentPage = ref(0);
const itemCount = ref(0);

function move(movement: number) {
	if (movement > -1 && movement < itemCount.value) {
		content.value.children[currentPage.value].style.display = 'none';
		currentPage.value = movement;
		content.value.children[currentPage.value].style.display = 'flex';
	}
}

onMounted(() => {
	itemCount.value = content.value?.children.length ?? 0;
	if (itemCount.value > 0) move(0);
});
</script>

<style scoped>
figure {
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	height: 9.5rem;
}

.content {
	display: flex;
	flex: 1;
	justify-content: center;
	align-items: center;
	overflow: hidden;
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
}

.content > :deep(*) {
	display: none;
	background-color: var(--surface-section);
	padding: 2px;

	/* overflow: auto; */
	overflow-wrap: break-word;
	margin: auto 0;
	font-size: 10px;
}

.content > :deep(img) {
	/* height: fit-content; */
	margin: auto 0;
	object-fit: contain;
	max-height: 5rem;
}

:deep(a) {
	color: var(--primary-color);
}

nav {
	text-align: center;
	color: var(--text-color-subdued);

	& > ul {
		display: flex;
		align-items: center;
		list-style: none;
		justify-content: center;
		gap: 0.5rem;

		& > li {
			cursor: pointer;
			transition: 0.2s;
			border-radius: var(--border-radius);
			&:hover {
				background-color: var(--surface-highlight);
			}
		}

		&:not(.numeric) {
			margin: 1rem;
		}

		&:not(.numeric) > li {
			background-color: #dcdcdc;
			width: 0.5rem;
			height: 0.5rem;

			&.selected {
				background-color: var(--primary-color);
			}
		}

		&.numeric {
			gap: 0;
		}

		&.numeric > li {
			white-space: nowrap;
			padding: 0.25rem;

			&.selected {
				font-weight: var(--font-weight-semibold);
				color: var(--text-color-primary);
			}
		}
	}

	/* May be potentially used later */
	& .pi-arrow-left,
	& .pi-arrow-right {
		border-radius: 24px;
		font-size: 10px;
	}
}
</style>
