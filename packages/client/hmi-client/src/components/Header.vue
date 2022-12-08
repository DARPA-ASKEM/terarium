<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import Button from '@/components/Button.vue';
import IconSearchLocate16 from '@carbon/icons-vue/es/search--locate/16';
import { useCurrentRouter } from '@/router/index';
import { Project } from '@/types/Project';
import useResourcesStore from '@/stores/resources';

const emit = defineEmits(['show-data-explorer']);
const router = useRouter();
const { isCurrentRouteHome } = useCurrentRouter();
const isHome = computed(() => isCurrentRouteHome.value);

const resources = useResourcesStore();

const goToHomepage = () => {
	resources.setActiveProject(null);
	resources.activeProjectAssets = null;
	router.push('/');
};
const goToDataExplorer = () => emit('show-data-explorer');

defineProps<{
	projectName?: Project['name'];
}>();
</script>

<template>
	<header>
		<img v-if="isHome" src="@assets/images/logo.png" height="32" width="128" alt="logo" />
		<img v-else src="@assets/images/icon.png" height="32" width="32" alt="TERArium icon" />
		<p v-if="!isHome">
			<a @click="goToHomepage">Projects</a>
			<span>{{ projectName }}</span>
		</p>
		<aside>
			<Button class="data-explorer" @click="goToDataExplorer">
				<IconSearchLocate16 />
			</Button>
		</aside>
	</header>
</template>

<style scoped>
header {
	align-items: center;
	background-color: var(--un-color-body-surface-primary);
	box-shadow: var(--un-box-shadow-small);
	display: flex;
	gap: 2rem;
	min-height: var(--header-height);
	padding: 0.5rem 1rem;
}

p {
	align-items: center;
	display: flex;
	font-size: var(--un-font-xlarge);
}

p > * + *::before {
	content: '>';
	margin: 0 1rem;
}

p a {
	text-decoration: underline;
}

p a:hover,
p a:focus {
	color: var(--un-color-accent-dark);
}

aside {
	display: flex;
	margin-left: auto;
	/* Push it to the far side */
	gap: 1rem;
}

button.data-explorer {
	background-color: var(--un-color-accent);
}
</style>
