<template>
	<section class="avatar">
		<Avatar :label="userInitials" class="avatar m-2" shape="circle" />
	</section>
	<section>
		<span class="name">{{ user.name }} {{ isYou() }}</span>
		<span class="email">{{ user.email }}</span>
	</section>
	<section v-if="isAuthor" class="permissions">Author</section>
	<section v-else class="permissions">
		<Dropdown v-model="selectedPermission" :options="permissions" class="sm" />
	</section>
</template>

<script setup lang="ts">
import Avatar from 'primevue/avatar';
import { computed, ref, watch } from 'vue';
import useAuthStore from '@/stores/auth';
import Dropdown from 'primevue/dropdown';
import { User } from './tera-share-project.vue';

const props = defineProps<{ user: User; isAuthor: boolean }>();
const emit = defineEmits(['remove-user']);

const auth = useAuthStore();

const selectedPermission = ref('Edit');
const permissions = ref(['Edit', 'Write only', 'Remove access']);
const userInitials = computed(() =>
	props.user.name
		.split(' ')
		.reduce((accumulator, currentValue) => accumulator.concat(currentValue.substring(0, 1)), '')
);

function isYou() {
	return auth.name === props.user.name ? '(you)' : '';
}

watch(
	() => selectedPermission.value,
	() => {
		if (selectedPermission.value === 'Remove access') {
			emit('remove-user');
		}
	}
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	justify-content: center;
}
.name {
	font-weight: var(--font-weight-semibold);
}

.email {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.permissions {
	color: var(--text-color-subdued);
	margin-left: auto;
}
</style>
