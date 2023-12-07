<template>
	<Portal :appendTo="appendTo">
		<div
			class="tera-context-menu"
			ref="container"
			:style="{ height: `${model.length * 36}px`, width: '200px' }"
		>
			<TieredMenu v-if="isVisible" :model="model" class="p-tieredmenu-overlay">
				<template #item="{ item }">
					<a class="p-menuitem-link" @click="itemClick(item)">
						<span :class="item.icon" class="p-menuitem-icon" />
						<span class="p-menuitem-text">{{ item.label }}</span>
						<span v-if="!isEmpty(item.items)" class="pi pi-angle-right p-submenu-icon" />
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
	if (!container.value) return;

	pageX = event.pageX;
	pageY = event.pageY;

	isVisible.value = true;
	setPosition();

	event.stopPropagation();
	event.preventDefault();

	ZIndexUtils.set('menu', container.value, props.baseZIndex + DEFAULT_ZINDEX_MENU);
};
const hide = () => {
	if (!container.value) return;

	isVisible.value = false;
	ZIndexUtils.clear(container.value);
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
	if (!container.value) return;

	let left = pageX + 1;
	let top = pageY + 1;
	const width = container.value.offsetParent
		? container.value.offsetWidth
		: DomHandler.getHiddenElementOuterWidth(container.value);
	const height = container.value.offsetParent
		? container.value.offsetHeight
		: DomHandler.getHiddenElementOuterHeight(container.value);
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

	container.value.style.left = `${left}px`;
	container.value.style.top = `${top}px`;
};

const CLICK_EVENT = 'click';
// adapted from `outsideClickListener` method of primevue/contextmenu
const outsideClickListener = (event) => {
	const isOutsideContainer = container.value && !container.value.contains(event.target);

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

	&:deep(.p-menuitem-link:not(.p-disabled):hover) {
		background-color: var(--surface-highlight);
	}

	&:deep(.pi-angle-right) {
		color: var(--text-color-subdued);
	}
}
</style>
