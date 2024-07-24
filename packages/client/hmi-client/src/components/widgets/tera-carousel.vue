<template>
	<figure>
		<div ref="content" class="content" :style="{ height, width }">
			<slot />
			<div v-if="!hasSlot('default')" class="empty">
				<i class="pi pi-image" />
			</div>
		</div>
		<nav v-if="itemCount > 0">
			<ul :class="{ numeric: isNumeric || !isEmpty(labels) }">
				<li
					v-for="(_, index) in itemCount"
					:class="{ selected: index === currentPage }"
					:key="_"
					@click.stop="move(index)"
				>
					<span v-if="isNumeric">{{ index + 1 }}</span>
					<span v-else>{{ labels[index] }}</span>
				</li>
				<li v-if="isNumeric && itemCount > 5">(+{{ itemCount }})</li>
			</ul>
		</nav>
		<!-- FIXME: If we are going to use this consider making the chevron navigations work (they should positioned properly)
		<Button
			v-if="itemCount > 1"
			text
			severity="secondary"
			icon="pi pi-chevron-left"
			@click.stop="move(currentPage - 1)"
			class="back"
		/>
		<Button
			v-if="itemCount > 1"
			text
			severity="secondary"
			icon="pi pi-chevron-right"
			@click.stop="move(currentPage + 1)"
			class="forward"
		/> -->
	</figure>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, onMounted, useSlots } from 'vue';
// import Button from 'primevue/button';

defineProps({
	height: {
		type: String,
		default: null
	},
	width: {
		type: String,
		default: null
	},
	isNumeric: {
		type: Boolean,
		default: false
	},
	labels: {
		type: Array,
		default: () => []
	}
});

const content = ref();
const currentPage = ref(0);
const itemCount = ref(0);

const slots = useSlots();
const hasSlot = (name: string) => !!slots[name];

function move(movement: number) {
	if (movement > -1 && movement < itemCount.value) {
		content.value.children[currentPage.value].style.display = 'none';
		currentPage.value = movement;
		content.value.children[currentPage.value].style.display = 'block';
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
	justify-content: space-between;
	position: relative;
}

.content {
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
}

.content > :deep(*) {
	display: none;
	font-size: 10px;
	overflow-wrap: break-word;
	overflow: auto;
	background-color: var(--surface-section);
	padding: 4px;
}

.content > :deep(img) {
	background-color: transparent;
	height: 100%;
	width: 100%;
	object-fit: scale-down;
	padding: 0;
}

.empty {
	background-color: transparent;
}

:deep(a),
:deep(a:hover) {
	color: var(--primary-color);
}

i {
	margin: auto;
	color: rgb(226, 227, 229);
	font-size: 3rem;
}

nav {
	text-align: center;
	display: flex;
	overflow: auto;
	color: var(--text-color-subdued);

	& > ul {
		display: flex;
		flex-direction: row;
		align-items: center;
		list-style: none;
		justify-content: center;

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
			gap: 0.5rem;
		}

		&:not(.numeric) > li {
			background-color: #dcdcdc;
			width: 0.5rem;
			height: 0.5rem;
			/* Hides page number */
			& > span {
				display: none;
			}
			&.selected {
				background-color: var(--primary-color);
			}
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

	/* Part of above FIXME
	&.p-button {
		position: absolute;
		top: 1.25rem;
		opacity: 0;
		height: 5rem;
		&:hover {
			opacity: 0.5;
		}
	}

	&.back {
		left: 8px;
	}
	&.forward {
		right: 8px;
	}*/
}
</style>
