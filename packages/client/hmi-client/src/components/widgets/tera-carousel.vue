<template>
	<figure>
		<div ref="content" class="content">
			<slot />
		</div>
		<nav v-if="itemCount > 0">
			<ul>
				<li v-for="(_, index) in itemCount" :key="_" @click.stop="move(index)">
					<i :class="index === currentPage ? 'asset-count-selected-text' : 'asset-count-selected'">
						{{ index + 1 }}
					</i>
				</li>
				<!--Should appear-->
				<li v-if="itemCount > 5" class="asset-count-text">(+{{ itemCount }})</li>
			</ul>
		</nav>
	</figure>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

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

		& > li {
			white-space: nowrap;
			padding: 0.25rem;
			border-radius: var(--border-radius);

			&:hover {
				background-color: var(--surface-highlight);
			}

			& .asset-count-selected-text {
				font-weight: var(--font-weight-semibold);
				color: var(--text-color-primary);
			}
		}
	}
}
</style>
