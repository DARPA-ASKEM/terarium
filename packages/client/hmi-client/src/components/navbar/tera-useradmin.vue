<template>
	<main>
		<header>
			<h4>Administration</h4>
			<SelectButton v-model="view" :options="views" />
		</header>
		<DataTable v-if="view === View.USER && !users" class="p-datatable-sm user" :value="Array.from(Array(10).keys())">
			<Column header="Email">
				<template #body>
					<Skeleton></Skeleton>
				</template>
			</Column>
			<Column header="First name">
				<template #body>
					<Skeleton></Skeleton>
				</template>
			</Column>
			<Column header="Last name">
				<template #body>
					<Skeleton></Skeleton>
				</template>
			</Column>
			<Column header="Roles">
				<template #body>
					<Skeleton></Skeleton>
				</template>
			</Column>
		</DataTable>
		<DataTable
			v-if="view === View.USER && users"
			:value="users"
			v-model:selection="selectedUserRow"
			selectionMode="single"
			dataKey="id"
			@rowSelect="onUserRowSelect"
			scrollable
			scrollHeight="flex"
			class="p-datatable-sm user"
			sortMode="multiple"
		>
			<Column field="email" header="Email" sortable></Column>
			<Column field="firstName" header="First name" sortable></Column>
			<Column field="lastName" header="Last name" sortable></Column>
			<Column header="Roles">
				<template #body="slotProps">
					<div v-if="selectedId === slotProps.data.id">
						<MultiSelect
							v-model="selectedRoles"
							:options="systemRoles"
							optionLabel="name"
							@change="updateRoles"
							:loading="loadingId === selectedId"
						/>
					</div>
					<div v-else>
						{{ getRoleNames(slotProps.data.roles).toString() }}
					</div>
				</template>
			</Column>
		</DataTable>
		<DataTable
			v-if="view === View.GROUP && !groupTableData"
			class="p-datatable-sm group"
			:value="Array.from(Array(10).keys())"
		>
			<Column header="Name">
				<template #body>
					<Skeleton></Skeleton>
				</template>
			</Column>
		</DataTable>
		<DataTable
			v-if="view === View.GROUP && groupTableData"
			v-model:expandedRows="expandedRows"
			:value="groupTableData"
			dataKey="id"
			scrollable
			scrollHeight="flex"
			class="p-datatable-sm group"
			selectionMode="single"
			@row-expand="onRowExpand"
		>
			<Column expander />
			<Column field="name" header="Name" sortable></Column>
			<template #expansion="groupSlotProps">
				<DataTable
					v-if="!groupSlotProps.data.permissionRelationships?.permissionUsers"
					:value="Array.from(Array(10).keys())"
					id="user"
				>
					<Column header="Email">
						<template #body>
							<Skeleton></Skeleton>
						</template>
					</Column>
					<Column header="First name">
						<template #body>
							<Skeleton></Skeleton>
						</template>
					</Column>
					<Column header="Last name">
						<template #body>
							<Skeleton></Skeleton>
						</template>
					</Column>
					<Column header="Permission">
						<template #body>
							<Skeleton></Skeleton>
						</template>
					</Column>
				</DataTable>
				<DataTable
					v-if="groupSlotProps.data.permissionRelationships?.permissionUsers"
					:value="groupSlotProps.data.permissionRelationships.permissionUsers"
					id="user"
					v-model:selection="selectedGroupUser"
					selectionMode="single"
					@row-select="onGroupUserRowSelect"
				>
					<Column field="email" header="Email" sortable></Column>
					<Column field="firstName" header="First name" sortable></Column>
					<Column field="lastName" header="Last name" sortable></Column>
					<Column header="Permission">
						<template #body="groupUserSlotProps">
							<section class="group-user-end-col">
								<div v-if="selectedGroupUser && selectedGroupUser.id === groupUserSlotProps.data.id">
									<Dropdown
										v-model="selectedGroupRelationship"
										:options="groupRelationships"
										@change="(event) => updateGroupUserRelationship(event, groupSlotProps.data.id)"
										:loading="loadingId === selectedGroupUser.id"
									/>
								</div>
								<div v-else>
									{{ groupUserSlotProps.data.relationship }}
								</div>
								<Button
									icon="pi pi-times"
									rounded
									text
									class="p-button-icon-only remove-user"
									@click="onRemoveUser(groupSlotProps.data.id, groupUserSlotProps.data.id)"
								/>
							</section>
						</template>
					</Column>
				</DataTable>
				<section class="add-user">
					<Dropdown
						v-model="userDropdownValue"
						:options="usersMenu"
						optionLabel="name"
						editable
						placeholder="Add user to group"
						class="w-full"
						@update:model-value="(value) => onSelectUser(value.id)"
					/>
					<Button
						icon="pi pi-user"
						label="Add user"
						class="p-button"
						:disabled="!selectedUser"
						@click="addSelectedUserToGroup(groupSlotProps.data.id)"
					/>
				</section>
			</template>
		</DataTable>
		<Dialog v-model:visible="isRemoveUserDialogVisible" modal header="Remove user">
			<p>Are you sure you want to remove this user from the group?</p>
			<template #footer>
				<Button label="Cancel" class="p-button-secondary" @click="isRemoveUserDialogVisible = false" />
				<Button label="Remove project" @click="removeUserCallback" />
			</template>
		</Dialog>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import DataTable, { DataTableRowSelectEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import MultiSelect from 'primevue/multiselect';
import SelectButton from 'primevue/selectbutton';
import type { PermissionGroup, PermissionUser } from '@/types/Types';
import {
	getAllGroups,
	getGroup,
	addGroupUserPermissions,
	updateGroupUserPermissions,
	removeGroupUserPermissions
} from '@/services/groups';
import Button from 'primevue/button';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';
import { useToastService } from '@/services/toast';
import Dialog from 'primevue/dialog';
import Skeleton from 'primevue/skeleton';
import { getUsers, addRole, removeRole } from '@/services/user';
import { getRoles } from '@/services/roles';

interface Role {
	id: string;
	name: string;
}

enum View {
	USER = 'User',
	GROUP = 'Group'
}

// User admin table
const systemRoles = ref<Role[]>([]);
const users = ref<PermissionUser[] | null>(null);
const selectedId = ref();
const selectedUserRow = ref();
const selectedRoles = ref<Role[]>([]);

// Group admin table
const groupTableData = ref<PermissionGroup[] | null>(null);
const expandedRows = ref([]);
const userDropdownValue = ref('');
const selectedUser = ref<PermissionUser | null>();
const usersMenu = computed(() =>
	users.value?.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
);
const selectedGroupUser = ref<PermissionUser | null>(null);
const selectedGroupRelationship = ref('');
let currentGroupRelationship = '';
const groupRelationships = ref(['admin', 'member']);
const loadingId = ref<string | null>(null);

const isRemoveUserDialogVisible = ref(false);
let removeUserCallback;

const view = ref(View.USER);
const views = [View.USER, View.GROUP];

const onUserRowSelect = (event: DataTableRowSelectEvent) => {
	if (selectedId.value !== event.data.id) {
		selectedId.value = event.data.id;
		selectedRoles.value = event.data.roles;
	}
};

function getRoleNames(roles: Role[]) {
	const names: string[] = [];
	if (roles) {
		roles.forEach((role) => {
			names.push(role.name);
		});
	}
	return names;
}

const updateRoles = async () => {
	if (selectedId.value) {
		loadingId.value = selectedId.value;
		const existingRoles = users.value?.find((user) => user.id === selectedId.value)?.roles;
		const rolesToAdd =
			selectedRoles.value.filter(({ name }) => !existingRoles?.map((role) => role.name).includes(name)) ?? [];
		const rolesToRemove =
			existingRoles?.filter(({ name }) => !selectedRoles.value.map((role) => role.name).includes(name)) ?? [];
		await Promise.all(rolesToAdd.map((role) => addRole(selectedId.value, role.name)));
		await Promise.all(rolesToRemove.map((role) => removeRole(selectedId.value, role.name)));
		users.value = await getUsers();
		selectedUserRow.value = null;
		loadingId.value = null;
	}
};

const onRowExpand = async (event) => {
	const selectedGroup = event.data as PermissionGroup;
	getAndPopulateGroup(selectedGroup.id);
};

const getAndPopulateGroup = async (groupId: string) =>
	getGroup(groupId).then((group) => {
		if (group) {
			groupTableData.value?.forEach((g) => {
				if (g.id === group.id) {
					g.permissionRelationships = group.permissionRelationships;
				}
			});
		}
		return group;
	});

const onSelectUser = (userId: string) => {
	const found = users.value?.find(({ id }) => id === userId);
	if (found) {
		selectedUser.value = found;
	}
};

const addSelectedUserToGroup = async (groupId: string) => {
	if (selectedUser.value?.id) {
		const added = await addGroupUserPermissions(groupId, selectedUser.value.id, 'member');
		if (added) {
			getAndPopulateGroup(groupId).then(() => {
				loadingId.value = null;
				selectedGroupUser.value = null;
				useToastService().success('', 'User added to group');
			});
		} else {
			useToastService().error('', 'Failed to add user to group');
		}
	}
};

const onGroupUserRowSelect = (event: DataTableRowSelectEvent) => {
	selectedGroupRelationship.value = groupRelationships.value.find((r) => r === event.data.relationship) ?? '';
	currentGroupRelationship = selectedGroupRelationship.value;
};

const updateGroupUserRelationship = (event: DropdownChangeEvent, groupId: string) => {
	if (selectedGroupUser.value) {
		loadingId.value = selectedGroupUser.value.id;
		updateGroupUserPermissions(groupId, selectedGroupUser.value?.id, currentGroupRelationship, event.value).then(
			(response) => {
				if (response) {
					getAndPopulateGroup(groupId).then(() => {
						loadingId.value = null;
						selectedGroupUser.value = null;
						useToastService().success('', 'User group permission updated');
					});
				} else {
					useToastService().error('', 'Failed to update user group permission');
				}
			}
		);
	}
};

const removeUserFromGroup = async (groupId: string, userId: string) => {
	const relationship = groupTableData.value
		?.find((g) => g.id === groupId)
		?.permissionRelationships?.permissionUsers?.find((u) => u.id === userId)?.relationship;
	if (relationship) {
		const removed = await removeGroupUserPermissions(groupId, userId, relationship);
		if (removed) {
			getAndPopulateGroup(groupId).then(() => {
				useToastService().success('', 'User removed from group');
			});
		} else {
			useToastService().error('', 'Failed to remove user from group');
		}
	}
	isRemoveUserDialogVisible.value = false;
};

const onRemoveUser = (groupId: string, userId: string) => {
	removeUserCallback = () => removeUserFromGroup(groupId, userId);
	isRemoveUserDialogVisible.value = true;
};

watch(
	() => view.value,
	async () => {
		if (view.value === View.USER) {
			users.value = await getUsers();
			const roles = await getRoles();
			systemRoles.value = roles?.map(({ id, name }) => ({ id, name })) ?? [];
		} else if (view.value === View.GROUP) {
			groupTableData.value = await getAllGroups();
			if (!users.value) {
				getUsers();
			}
		}
	}
);

onMounted(async () => {
	const roles = await getRoles();
	systemRoles.value = roles?.map(({ id, name }) => ({ id, name })) ?? [];
	users.value = await getUsers();
	groupTableData.value = await getAllGroups();
});
</script>

<style scoped>
main {
	display: flex;
	flex-direction: column;
	padding: 1rem 1rem 0 1rem;
}

header {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.add-user {
	display: flex;
}

.group-user-end-col {
	display: flex;
	justify-content: space-between;
}

.p-button.p-button-text.remove-user:hover {
	color: var(--text-color-danger);
}

.p-datatable.user:deep(.p-datatable-tbody > tr),
.p-datatable.user:deep(.p-datatable-thead > tr) {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
}

.p-datatable#user:deep(.p-datatable-tbody > tr),
.p-datatable#user:deep(.p-datatable-thead > tr) {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
}

.p-datatable.group:deep(.p-datatable-tbody > tr:not(.p-datatable-row-expansion)),
.p-datatable.group:deep(.p-datatable-thead > tr) {
	display: flex;
}

.p-datatable.group:deep(.p-datatable-tbody > tr > td:nth-child(2)),
.p-datatable.group:deep(.p-datatable-thead > tr > th:nth-child(2)) {
	flex-grow: 1;
}

.p-datatable:deep(.p-datatable-tbody > tr.p-datatable-row-expansion > td) {
	background: var(--surface-100);
}

.p-datatable:deep(.p-datatable-tbody > tr > td),
.p-datatable:deep(.p-datatable-thead > tr > th) {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: top;
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	padding: 1rem 0.5rem;
	background-color: var(--surface-ground);
}

.p-datatable:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td) {
	color: var(--text-color-secondary);
}

.p-datatable.p-datatable-sm:deep(.p-datatable-tbody > tr > td) {
	padding: 1rem;
}

.p-datatable:deep(.p-datatable-tbody > tr > td:not(:last-child)) {
	padding-top: 1rem;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
	cursor: pointer;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}

.p-datatable:deep(.p-datatable-tbody > tr.p-highlight) {
	background: var(--surface-ground);
}

.p-multiselect,
.p-dropdown {
	height: 2.5rem;
}

.p-multiselect:deep(.p-multiselect-label) {
	padding: 0.5rem;
}
</style>
