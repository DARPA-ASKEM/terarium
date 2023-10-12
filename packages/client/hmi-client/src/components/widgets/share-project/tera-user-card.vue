<template>
	<section class="avatar">
		<Avatar :label="userInitials" class="avatar m-2" shape="circle" />
	</section>
	<section>
		<span class="name">{{ user.givenName }} {{ user.familyName }} {{ isYou() }}</span>
		<span class="email">{{ user.email }}</span>
	</section>
	<section v-if="isAuthor" class="permissions">Author</section>
	<section v-else class="permissions">
		<Dropdown
			v-model="selectedPermission"
			:options="permissions"
			class="sm"
			@change="selectPermission"
		/>
	</section>
</template>

<script setup lang="ts">
import Avatar from 'primevue/avatar';
import { computed, ref, watch } from 'vue';
import useAuthStore from '@/stores/auth';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';
import { User } from '@/types/Types';

const props = defineProps<{ user: User; permission?: string }>();
const emit = defineEmits(['select-permission']);

const auth = useAuthStore();

const selectedPermission = ref('Edit');
const permissions = ref(['Edit', 'Read only', 'Remove access']);
const userInitials = computed(() =>
	props.user.givenName.charAt(0).concat(props.user.familyName.charAt(0))
);
const isAuthor = computed(() => props.permission === 'creator');

function isYou() {
	return auth.user?.name === props.user.givenName ? '(you)' : '';
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
