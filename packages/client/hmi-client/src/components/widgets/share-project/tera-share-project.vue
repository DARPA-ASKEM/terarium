<template>
	<Dialog
		v-model:visible="visible"
		modal
		:closable="false"
		header="Share project"
		@hide="$emit('update:modelValue', false)"
		@after-hide="onAfterHide"
		:style="{ width: '30rem' }"
	>
		<section class="container">
			<Dropdown
				v-model="selectedUser"
				:options="usersMenu"
				optionLabel="name"
				editable
				placeholder="Add people and groups"
				@update:model-value="(value) => addSelectedUser(value.id)"
				class="w-full sm"
			/>
			<section class="selected-users" v-if="selectedUsers.size > 0">
				<h6>People and groups with access</h6>
				<ul>
					<li v-for="(user, i) in selectedUsers" :key="i">
						<tera-user-card
							:user="user"
							:is-author="isUserAuthor(user)"
							@remove-user="removeUserAccess(user)"
						/>
					</li>
				</ul>
			</section>
			<section>
				<h6>General access</h6>
				<Dropdown v-model="generalAccess" :options="generalAccessOptions" class="sm">
					<template #value="slotProps">
						<div class="general-access-option">
							<i :class="slotProps.value.icon" />
							<span>{{ slotProps.value.label }}</span>
						</div>
					</template>
					<template #option="slotProps">
						<div class="general-access-option">
							<i :class="slotProps.option.icon" />
							<span>{{ slotProps.option.label }}</span>
						</div>
					</template>
				</Dropdown>
				<div class="caption">{{ generalAccessCaption }}</div>
			</section>
		</section>
		<template #footer>
			<Button label="Cancel" class="p-button-secondary xsm" @click="visible = false" />
			<Button label="Done" class="xsm" @click="shareProject" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import { watch, ref, computed } from 'vue';
import Button from 'primevue/button';
import { IProject } from '@/types/Project';
import { getUsers } from '@/services/user';
import { PermissionUser, PermissionRelationships } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraUserCard from './tera-user-card.vue';

const props = defineProps<{ modelValue: boolean; project: IProject }>();

const visible = ref(props.modelValue);
const permissions = ref<PermissionRelationships | null>(null);
const users = ref<PermissionUser[]>([]);
const usersMenu = computed(() =>
	users.value.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
);
const selectedUser = ref(null);
const selectedUsers = ref<Set<PermissionUser>>(new Set());
const generalAccessOptions = ref([
	{ label: 'Restricted', icon: 'pi pi-lock' },
	{ label: 'Public', icon: 'pi pi-users' }
]);
const generalAccess = ref(generalAccessOptions.value[0]);
const generalAccessCaption = computed(() => {
	if (generalAccess.value.label === 'Restricted') {
		return 'Only people with access can view, edit, and copy this project.';
	}
	return 'Anyone can view and copy this project.';
});

function addSelectedUser(id) {
	if (id) {
		const user = users.value.find((u) => u.id === id);
		if (user) {
			selectedUsers.value.add(user);
		}
	}
}

function onAfterHide() {
	selectedUser.value = null;
	selectedUsers.value = new Set();
}

function removeUserAccess(user: PermissionUser) {
	selectedUsers.value.delete(user);
	selectedUser.value = null;
}

function shareProject() {
	visible.value = false;
}

function isUserAuthor(user: PermissionUser) {
	return permissions.value?.users.find(
		(pUser) => pUser.id === user.id && pUser.relationship === 'creator'
	);
}

watch(
	() => props.project,
	async () => {
		users.value = (await getUsers()) ?? [];
		permissions.value = await useProjects().getPermissions(props.project.id);
		const author = permissions.value?.users.find((user) => user.relationship === 'creator');
		if (author) {
			addSelectedUser(author.id);
		}
	},
	{ immediate: true }
);

watch(
	() => props.modelValue,
	() => {
		visible.value = props.modelValue;
	}
);
</script>

<style scoped>
.container {
	margin-right: 0.5rem;
}

section {
	padding-top: 0.5rem;
}

section > section {
	padding-top: 1rem;
}

h6 {
	margin-bottom: 0.5rem;
}

.selected-users {
	display: flex;
	flex-direction: column;
}

li {
	list-style: none;
	display: flex;
	gap: 0.5rem;
}

.general-access-option {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

.caption {
	color: var(--text-color-subdued);
	font-size: var(--font-tiny);
}

.p-dropdown.sm {
	margin-bottom: 0.5rem;
}
</style>
