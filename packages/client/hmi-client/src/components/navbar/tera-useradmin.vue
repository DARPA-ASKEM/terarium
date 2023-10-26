<template>
	<main>
		<header>
			<h4>Administration</h4>
			<SelectButton v-model="view" :options="views" />
		</header>
		<DataTable
			v-if="view === View.USER"
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
						/>
					</div>
					<div v-else>
						{{ getRoleNames(slotProps.data.roles).toString() }}
					</div>
				</template>
			</Column>
		</DataTable>
		<DataTable
			v-else-if="view === View.GROUP"
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
					:value="groupSlotProps.data.permissionRelationships.permissionUsers"
					id="user"
					v-model:selection="selectedGroupUser"
					selectionMode="single"
					@row-select="onGroupUserRowSelect"
				>
					<Column field="email" header="Email" sortable></Column>
					<Column field="firstName" header="First name" sortable></Column>
					<Column field="lastName" header="Last name" sortable></Column>
					<!-- <Column field="relationship" header="Permission" sortable></Column> -->
					<Column>
						<template #body="groupUserSlotProps">
							<div v-if="selectedGroupUser && selectedGroupUser.id === groupUserSlotProps.data.id">
								<Dropdown
									v-model="selectedGroupRelationship"
									:options="groupRelationships"
									@change="(event) => updateGroupUserRelationship(event, groupSlotProps.data.id)"
								/>
							</div>
							<div v-else>
								{{ groupUserSlotProps.data.relationship }}
							</div>
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
						@click="addSelectedUserToGroup(slotProps.data.id)"
					/>
				</section>
			</template>
		</DataTable>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import DataTable, { DataTableRowSelectEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import API from '@/api/api';
import MultiSelect from 'primevue/multiselect';
import SelectButton from 'primevue/selectbutton';
import { PermissionGroup, PermissionUser } from '@/types/Types';
import {
	getAllGroups,
	getGroup,
	addGroupUserPermissions,
	updateGroupUserPermissions
} from '@/services/groups';
import Button from 'primevue/button';
import Dropdown, { DropdownChangeEvent } from 'primevue/dropdown';

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
const users = ref();
const selectedId = ref();
const selectedUserRow = ref();
const selectedRoles = ref<Role[]>([]);

// Group admin table
const groupTableData = ref<PermissionGroup[] | null>(null);
const expandedRows = ref([]);
const userDropdownValue = ref('');
const selectedUser = ref<PermissionUser | null>();
const usersMenu = computed(() =>
	users.value.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
);
const selectedGroupUser = ref<PermissionUser | null>(null);
const selectedGroupRelationship = ref('');
const groupRelationships = ref(['admin', 'member']);

const view = ref(View.USER);
const views = [View.USER, View.GROUP];

const getRoles = async () => {
	try {
		const response = await API.get('/roles');
		if (response.status >= 200 && response.status < 300 && response.data) {
			systemRoles.value = response.data.map(({ id, name }) => ({ id, name }));
		}
	} catch (err) {
		console.log(err);
	}
	return null;
};
const getUsers = async () => {
	try {
		const response = await API.get('/users');
		if (response.status >= 200 && response.status < 300) {
			users.value = response.data;
		}
	} catch (err) {
		console.log(err);
	}
	return null;
};

const onUserRowSelect = (event: DataTableRowSelectEvent) => {
	selectedId.value = event.data.id;
	selectedRoles.value = event.data.roles;
};

const addRole = async (role) => {
	try {
		const response = await API({
			url: `/users/${selectedId.value}/roles/${role.name}`,
			method: 'POST',
			validateStatus: (status: number) => status < 400 //
		});
		if (response.status >= 200 && response.status < 300) {
			await getUsers();
		}
	} catch (err) {
		console.log(err);
	}
};
const removeRole = async (role) => {
	try {
		const response = await API({
			url: `/users/${selectedId.value}/roles/${role.name}`,
			method: 'DELETE',
			validateStatus: (status: number) => status < 400 //
		});
		if (response.status >= 200 && response.status < 300) {
			getUsers();
		}
	} catch (err) {
		console.log(err);
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

const updateRoles = () => {
	if (selectedId.value) {
		const existingRoles = users.value.find((user) => user.id === selectedId.value).roles;
		const rolesToAdd =
			selectedRoles.value.filter(
				({ name }) => !existingRoles.map((role) => role.name).includes(name)
			) ?? [];
		const rolesToRemove =
			existingRoles.filter(
				({ name }) => !selectedRoles.value.map((role) => role.name).includes(name)
			) ?? [];
		rolesToAdd.forEach((role) => {
			addRole(role);
		});
		rolesToRemove.forEach((role) => {
			removeRole(role);
		});
	}
};

const onRowExpand = async (event) => {
	const selectedGroup = event.data as PermissionGroup;
	const group = await getGroup(selectedGroup.id);
	if (group) {
		groupTableData.value?.forEach((g) => {
			if (g.id === group.id) {
				g.permissionRelationships = group.permissionRelationships;
			}
		});
	}
};

const onSelectUser = (userId: string) => {
	const found = users.value.find(({ id }) => id === userId);
	if (found) {
		selectedUser.value = found;
	}
};

const addSelectedUserToGroup = (groupId: string) => {
	if (selectedUser.value?.id) {
		addGroupUserPermissions(groupId, selectedUser.value?.id, 'member');
	}
};

const onGroupUserRowSelect = (event: DataTableRowSelectEvent) => {
	selectedGroupRelationship.value =
		groupRelationships.value.find((r) => r === event.data.relationship) ?? '';
};

const updateGroupUserRelationship = (event: DropdownChangeEvent, groupId: string) => {
	if (selectedGroupUser.value) {
		updateGroupUserPermissions(
			groupId,
			selectedGroupUser.value?.id,
			selectedGroupRelationship.value,
			event.value
		);
	}
};

watch(
	() => view.value,
	async () => {
		getUsers();
		if (view.value === View.USER) {
			getRoles();
		} else if (view.value === View.GROUP) {
			groupTableData.value = await getAllGroups();
		}
	}
);

onMounted(async () => {
	getRoles();
	getUsers();
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
