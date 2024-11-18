<template>
	<Dialog
		modal
		header="Share project"
		style="width: 35rem"
		v-model:visible="visible"
		:closable="false"
		@after-hide="onAfterHide"
		@hide="$emit('update:modelValue', false)"
	>
		<main>
			<Dropdown
				v-model="selectedUser"
				:options="usersMenu"
				optionLabel="name"
				editable
				showClear
				placeholder="Add people and groups"
				@update:model-value="(value) => addNewSelectedUser(value?.id)"
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
				<Dropdown
					:model-value="generalAccess"
					:options="generalAccessOptions"
					class="p-dropdown-sm accessibility"
					:loading="isUpdatingAccessibility"
					@update:model-value="changeAccessibility"
					:disabled="isSample"
				>
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
				<div class="text-sm">{{ generalAccessCaption }}</div>
			</section>
			<section v-if="useAuthStore().isAdmin">
				<label for="sample-project" class="cursor-pointer block">
					<i v-if="isSampleLoading" class="pi pi-spin pi-spinner" style="color: var(--primary-color)" />
					<input v-else type="checkbox" id="sample-project" v-model="isSample" class="m-0" />
					<strong class="ml-2">Sample project</strong>
					<span class="block pt-1 text-sm">
						A sample project is public and only editable by an <em>administrator</em>.
					</span>
				</label>
			</section>
		</main>
		<template #footer>
			<Button label="Done" @click="onDone" />
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
import useAuthStore from '@/stores/auth';
import TeraUserCard from './tera-user-card.vue';

enum Accessibility {
	Restricted = 'Restricted',
	Public = 'Public'
}

const props = defineProps<{ modelValue: boolean; project: Project }>();

const visible = ref(props.modelValue);
const permissions = ref<PermissionRelationships | null>(null);
const users = ref<PermissionUser[]>([]);
const usersMenu = computed(() =>
	users.value
		.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
		.sort((a, b) => a.name.localeCompare(b.name))
);
const selectedUser = ref(null);
const existingUsers = ref<Set<PermissionUser>>(new Set());
const newSelectedUsers = ref<Set<PermissionUser>>(new Set());
const selectedUsers = computed(() => new Set([...existingUsers.value, ...newSelectedUsers.value]));
const existingUserPermissions: Map<string, string> = new Map();
const newSelectedUserPermissions: Map<string, string> = new Map();
const generalAccessOptions = ref([
	{ label: Accessibility.Restricted, icon: 'pi pi-lock' },
	{ label: Accessibility.Public, icon: 'pi pi-users' }
]);
const isUpdatingAccessibility = ref(false);
const publicGeneralAccess = ref(props.project.publicProject);
const generalAccess = computed(() => {
	if (isUpdatingAccessibility.value) return { label: 'Loading...' };

	return publicGeneralAccess.value ? generalAccessOptions.value[1] : generalAccessOptions.value[0];
});
const generalAccessCaption = computed(() => {
	if (generalAccess.value.label === Accessibility.Restricted) {
		return 'Only people with access can view, edit, and copy this project.';
	}
	return 'Anyone can view and copy this project.';
});

function onDone() {
	setPermissions();
}

async function changeAccessibility({ label }: { label: Accessibility }) {
	isUpdatingAccessibility.value = true;
	await useProjects().setAccessibility(props.project.id, label === Accessibility.Public);
	publicGeneralAccess.value = label === Accessibility.Public;
	isUpdatingAccessibility.value = false;
}

function addExistingUser(id: string, relationship?: string) {
	const user = users.value.find((u) => u.id === id);
	if (user) {
		existingUsers.value.add(user);
		if (relationship) {
			existingUserPermissions.set(id, relationship);
		}
	}
}

function addNewSelectedUser(id?: string) {
	if (!id) {
		return;
	}
	const user = users.value.find((u) => u.id === id);
	if (user) {
		newSelectedUsers.value.add(user);
		newSelectedUserPermissions.set(id, 'writer');

		selectedUser.value = null;
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
	await selectedUsers.value.forEach(async ({ id }) => {
		const permission = newSelectedUserPermissions.get(id);
		if (permission) {
			const currentPermission = permissions.value?.permissionUsers.find((u) => u.id === id)?.relationship;
			if (permission === 'remove') {
				if (currentPermission) {
					if (await useProjects().removePermissions(props.project.id, id, currentPermission)) {
						removeUser(id);
					} else {
						addUser(id);
					}
				}
			} else if (currentPermission) {
				if (await useProjects().updatePermissions(props.project.id, id, currentPermission, permission)) {
					if (permission === 'reader') {
						removeUser(id);
					} else {
						addUser(id);
					}
				}
			} else if (await useProjects().setPermissions(props.project.id, id, permission)) {
				if (permission === 'writer') {
					addUser(id);
				}
			}
		}
	});
	newSelectedUserPermissions.clear();
}

async function removeUser(id) {
	const user = users.value.find((u) => u.id === id);
	const name = `${user?.firstName} ${user?.lastName}`;
	const updatedProject = structuredClone(props.project);
	if (updatedProject.authors) {
		const index = updatedProject.authors.indexOf(name);
		if (index !== undefined && index > -1) {
			updatedProject.authors.splice(index, 1);
		}
		await useProjects().update(updatedProject);
	}
}

async function addUser(id) {
	const user = users.value.find((u) => u.id === id);
	const name = `${user?.firstName} ${user?.lastName}`;
	const updatedProject = structuredClone(props.project);
	if (updatedProject.authors) {
		updatedProject.authors.push(name);
	} else {
		updatedProject.authors = [name];
	}
	await useProjects().update(updatedProject);
}

async function getPermissions() {
	permissions.value = await useProjects().getPermissions(props.project.id);
	existingUsers.value = new Set();
	permissions.value?.permissionUsers.forEach(({ id, relationship }) => addExistingUser(id, relationship));
}

watch(
	() => props.project,
	async () => {
		existingUsers.value = new Set();
		users.value = (await getUsers()) ?? [];
		isSample.value = props.project.sampleProject ?? false;
		publicGeneralAccess.value = props.project.publicProject;
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

/**
 * Sample project
 */
const isSample = ref<boolean>(props.project.sampleProject ?? false);
const isSampleLoading = ref<boolean>(false);
watch(isSample, (value) => {
	// When the user decides to change the sample project status
	if (props.project.sampleProject !== value) {
		isSampleLoading.value = true;
		useProjects()
			.setSample(props.project.id, value)
			.then((done) => {
				isSampleLoading.value = false;
				// In case of error, revert the checkbox to the previous state
				if (!done) isSample.value = props.project.sampleProject ?? false;
			});
	}
});
</script>

<style scoped>
section + section {
	margin-top: var(--gap-3);
}

main > section {
	padding-top: var(--gap-4);
}

h6 {
	margin-bottom: var(--gap-2);
}

.accessibility {
	width: 10rem;
}

.selected-users {
	display: flex;
	flex-direction: column;
	padding-bottom: var(--gap-2);
}

li {
	list-style: none;
	display: flex;
	gap: var(--gap-2);
}

.general-access-option {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
}

.caption {
	color: var(--text-color-subdued);
	font-size: var(--font-tiny);
}

.p-dropdown {
	margin-bottom: var(--gap-2);
}
</style>
