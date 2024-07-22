<template>
	<section class="avatar">
		<Avatar :label="userInitials" class="avatar m-2" shape="circle" />
	</section>
	<section>
		<!-- TODO: replace 'firstName' and 'lastName' with 'givenName' and 'familyName' once staging data is updated -->
		<span class="name">{{ user['firstName'] }} {{ user['lastName'] }} {{ isYou() }}</span>
		<span class="email">{{ user.email }}</span>
	</section>
	<section v-if="isAuthor" class="permissions">Author</section>
	<section v-else class="permissions">
		<Dropdown v-model="selectedPermission" :options="permissions" class="p-dropdown-sm" @change="selectPermission" />
	</section>
</template>

<script setup lang="ts">
import Avatar from 'primevue/avatar';
import { computed, ref, watch } from 'vue';
import useAuthStore from '@/stores/auth';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';
import type { PermissionUser } from '@/types/Types';

const props = defineProps<{ user: PermissionUser; permission?: string }>();
const emit = defineEmits(['select-permission']);

const auth = useAuthStore();

const selectedPermission = ref('Edit');
const permissions = ref(['Edit', 'Read only', 'Remove access']);
const userInitials = computed(() => props.user.firstName.charAt(0).concat(props.user.lastName.charAt(0)));
const isAuthor = computed(() => props.permission === 'creator');

function isYou() {
	return auth.user?.id === props.user.id ? '(you)' : '';
}

function selectPermission(event: DropdownChangeEvent) {
	if (!isAuthor.value) {
		emit('select-permission', event.value);
	}
}

watch(
	() => props.permission,
	() => {
		if (props.permission === 'writer') {
			selectedPermission.value = 'Edit';
		} else if (props.permission === 'reader') {
			selectedPermission.value = 'Read only';
		}
	},
	{ immediate: true }
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
