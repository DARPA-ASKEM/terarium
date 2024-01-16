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
				@update:model-value="(value) => addNewSelectedUser(value.id)"
				class="w-full p-dropdown-sm"
			/>
			<section class="selected-users" v-if="selectedUsers.size > 0">
				<h6>People and groups with access</h6>
				<ul>
					<li v-for="(user, i) in selectedUsers" :key="i">
						<tera-user-card
							:user="user"
							:permission="existingUserPermissions.get(user.id)"
							@select-permission="(permission) => selectNewPermissionForUser(permission, user.id)"
						/>
					</li>
				</ul>
			</section>
			<section>
				<h6>General access</h6>
				<Dropdown v-model="generalAccess" :options="generalAccessOptions" class="p-dropdown-sm">
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
			<Button label="Done" class="xsm" @click="setPermissions" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import { watch, ref, computed } from 'vue';
import Button from 'primevue/button';
import { getUsers } from '@/services/user';
import type { PermissionRelationships, PermissionUser, Project } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraUserCard from './tera-user-card.vue';

const props = defineProps<{ modelValue: boolean; project: Project }>();

const visible = ref(props.modelValue);
const permissions = ref<PermissionRelationships | null>(null);
const users = ref<PermissionUser[]>([]);
const usersMenu = computed(() =>
	users.value.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
);
const selectedUser = ref(null);
const existingUsers = ref<Set<PermissionUser>>(new Set());
const newSelectedUsers = ref<Set<PermissionUser>>(new Set());
const selectedUsers = computed(() => new Set([...existingUsers.value, ...newSelectedUsers.value]));
const existingUserPermissions: Map<string, string> = new Map();
const newSelectedUserPermissions: Map<string, string> = new Map();
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

function addExistingUser(id: string, relationship?: string) {
	const user = users.value.find((u) => u.id === id);
	if (user) {
		existingUsers.value.add(user);
		if (relationship) {
			existingUserPermissions.set(id, relationship);
		}
	}
}

function addNewSelectedUser(id: string) {
	const user = users.value.find((u) => u.id === id);
	if (user) {
		newSelectedUsers.value.add(user);
		newSelectedUserPermissions.set(id, 'writer');
	}
}

function onAfterHide() {
	selectedUser.value = null;
	newSelectedUsers.value = new Set();
}

function selectNewPermissionForUser(permission: string, userId: string) {
	let permissionToSet;
	switch (permission) {
		case 'Edit':
			permissionToSet = 'writer';
			break;
		case 'Read only':
			permissionToSet = 'reader';
			break;
		default:
			permissionToSet = 'remove';
			break;
	}
	newSelectedUserPermissions.set(userId, permissionToSet);
}

async function setPermissions() {
	visible.value = false;
	selectedUsers.value.forEach(async ({ id }) => {
		const permission = newSelectedUserPermissions.get(id);
		if (permission) {
			const currentPermission = permissions.value?.permissionUsers.find((u) => u.id === id)
				?.relationship;
			if (permission === 'remove') {
				if (currentPermission) {
					await useProjects().removePermissions(props.project.id, id, currentPermission);
				}
			} else if (currentPermission) {
				await useProjects().updatePermissions(props.project.id, id, currentPermission, permission);
			} else {
				await useProjects().setPermissions(props.project.id, id, permission);
			}
		}
	});
	newSelectedUserPermissions.clear();
}

async function getPermissions() {
	permissions.value = await useProjects().getPermissions(props.project.id);
	existingUsers.value = new Set();
	permissions.value?.permissionUsers.forEach(({ id, relationship }) =>
		addExistingUser(id, relationship)
	);
}

watch(
	() => props.project,
	async () => {
		existingUsers.value = new Set();
		users.value = (await getUsers()) ?? [];
		console.log(users.value);
	},
	{ immediate: true }
);

watch(
	() => [visible.value, users.value],
	() => {
		if (visible.value && users.value) {
			getPermissions();
		}
	}
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

.p-dropdown {
	margin-bottom: 0.5rem;
}
</style>
