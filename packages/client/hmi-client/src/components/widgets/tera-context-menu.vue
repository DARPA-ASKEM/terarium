<template>
	<Portal :appendTo="props.appendTo">
		<div class="tera-context-menu" ref="container">
			<Menu v-if="isVisible" :model="props.model">
				<template v-slot:item="{ item }">
					<a class="p-menuitem-link" @click="(e) => itemClick(e, item)">
						<span class="p-menuitem-text">{{ item.label }}</span>
					</a>
				</template>
			</Menu>
		</div>
	</Portal>
</template>

<script setup lang="ts">
import { ref, onUnmounted, PropType } from 'vue';
import Portal from 'primevue/portal';
import Menu from 'primevue/menu';
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

const itemClick = (e, item: MenuItem) => {
	if (item.command) {
		hide();
		item.command(e);
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
}
.tera-context-menu:deep(.p-menu) {
	width: fit-content;
}
.tera-context-menu:deep(.p-submenu-header .p-menuitem-link) {
	padding-left: 0px;
	padding-bottom: 0px;
}

.tera-context-menu:deep(.p-submenu-header .p-menuitem-link):hover {
	background-color: var(--surface);
	cursor: default;
}
.tera-context-menu:deep(.p-submenu-header .p-menuitem-link .p-menuitem-text) {
	color: var(--text-color-secondary);
}
.tera-context-menu:deep(.p-submenu-header .p-menuitem-link .p-menuitem-text):hover {
	color: var(--text-color-secondary);
}
</style>
