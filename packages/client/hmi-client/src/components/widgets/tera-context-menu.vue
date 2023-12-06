<template>
	<Portal :appendTo="props.appendTo">
		<div class="tera-context-menu" ref="container">
			<TieredMenu v-if="isVisible" :model="props.model">
				<template #item="{ item }">
					<a class="p-menuitem-link" @click="itemClick(item)">
						<i :class="item.icon" class="menuitem-icon" />
						<span class="p-menuitem-text">{{ item.label }}</span>
						<i v-if="!isEmpty(item.items)" class="pi pi-angle-right ml-auto" />
					</a>
				</template>
			</TieredMenu>
		</div>
	</Portal>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, onUnmounted, PropType } from 'vue';
import Portal from 'primevue/portal';
import TieredMenu from 'primevue/tieredmenu';
import { MenuItem } from 'primevue/menuitem';
import { DomHandler, ZIndexUtils } from 'primevue/utils';

const props = defineProps({
	model: {
		type: Array as PropType<MenuItem[]>,
		default: null
	},
	appendTo: {
		type: String,
		required: false,
		default: 'body'
	},
	baseZIndex: {
		type: Number,
		required: false,
		default: 0
	}
});

const container = ref<HTMLElement>();
const isVisible = ref(false);
let pageX = 0;
let pageY = 0;

const DEFAULT_ZINDEX_MENU = 1000;

const show = (event) => {
	const containerVal = container.value;
	if (!containerVal) return;

	pageX = event.pageX;
	pageY = event.pageY;
	isVisible.value = true;
	setPosition();

	event.stopPropagation();
	event.preventDefault();

	ZIndexUtils.set('menu', containerVal, props.baseZIndex + DEFAULT_ZINDEX_MENU);
};
const hide = () => {
	const containerVal = container.value;
	if (!containerVal) return;

	isVisible.value = false;
	ZIndexUtils.clear(containerVal);
};
defineExpose({
	show,
	hide
});

const itemClick = (item: MenuItem) => {
	if (item.command) {
		hide();
	}
};

// adapted from `position` method of primevue/contextmenu
const setPosition = () => {
	const containerVal = container.value;
	if (!containerVal) return;

	let left = pageX + 1;
	let top = pageY + 1;
	const width = containerVal.offsetParent
		? containerVal.offsetWidth
		: DomHandler.getHiddenElementOuterWidth(containerVal);
	const height = containerVal.offsetParent
		? containerVal.offsetHeight
		: DomHandler.getHiddenElementOuterHeight(containerVal);
	const viewport = DomHandler.getViewport();

	// flip
	if (left + width - document.body.scrollLeft > viewport.width) {
		left -= width;
	}

	// flip
	if (top + height - document.body.scrollTop > viewport.height) {
		top -= height;
	}

	// fit
	if (left < document.body.scrollLeft) {
		left = document.body.scrollLeft;
	}

	// fit
	if (top < document.body.scrollTop) {
		top = document.body.scrollTop;
	}

	containerVal.style.left = `${left}px`;
	containerVal.style.top = `${top}px`;
};

const CLICK_EVENT = 'click';
// adapted from `outsideClickListener` method of primevue/contextmenu
const outsideClickListener = (event) => {
	const containerVal = container.value;
	const isOutsideContainer = containerVal && !containerVal.contains(event.target);

	if (isOutsideContainer) {
		hide();
	}
};
document.addEventListener(CLICK_EVENT, outsideClickListener);
onUnmounted(() => {
	document.removeEventListener(CLICK_EVENT, outsideClickListener);
	ZIndexUtils.clear(container.value as HTMLElement);
});
</script>

<style scoped>
.tera-context-menu {
	position: absolute;

	&:deep(.p-tieredmenu),
	&:deep(.p-submenu-list) {
		width: fit-content;
		white-space: nowrap;
	}

	&:deep(.menuitem-icon) {
		margin-right: 0.5rem;
	}

	&:deep(.p-menuitem-link:not(.p-disabled):hover) {
		background-color: var(--surface-highlight);
	}

	&:deep(.pi-angle-right) {
		color: var(--text-color-subdued);
	}
}

/* Unused */
.tera-context-menu:deep(.p-submenu-header .p-menuitem-link) {
	padding-left: 0px;
	padding-bottom: 0px;
}

.tera-context-menu:deep(.p-submenu-header .p-menuitem-link):hover {
	background-color: var(--surface);
	cursor: default;
}
.tera-context-menu:deep(.p-submenu-header .p-menuitem-link .p-menuitem-text) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
}
</style>
