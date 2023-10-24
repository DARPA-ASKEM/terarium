<template>
	<main>
		<header>
			<h4>Administration</h4>
			<SelectButton v-model="view" :options="views" />
		</header>
		<DataTable
			v-if="view === View.USER"
			:value="adminTableData"
			v-model:selection="selectedAdminRow"
			selectionMode="single"
			dataKey="id"
			@rowSelect="onRowSelect"
			scrollable
			scrollHeight="flex"
			class="p-datatable-sm"
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
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import DataTable, { DataTableRowSelectEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import API from '@/api/api';
import MultiSelect from 'primevue/multiselect';
import SelectButton from 'primevue/selectbutton';

interface Role {
	id: string;
	name: string;
}

enum View {
	USER = 'User',
	GROUP = 'Group'
}

const systemRoles = ref<Role[]>([]);
const adminTableData = ref();
const selectedId = ref();
const selectedAdminRow = ref();
const selectedRoles = ref<Role[]>([]);

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
			adminTableData.value = response.data;
		}
	} catch (err) {
		console.log(err);
	}
	return null;
};
const getGroups = async () => {
	try {
		const response = await API.get('/groups');
		if (response.status >= 200 && response.status < 300) {
			// adminTableData.value = response.data;
			console.log(response.data);
			response.data.forEach(async (r) => {
				const group = await API.get(`/groups/${r.id}`);
				console.log(group.data);
			});
		}
	} catch (err) {
		console.log(err);
	}
	return null;
};

const onRowSelect = (event: DataTableRowSelectEvent) => {
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
		const existingRoles = adminTableData.value.find((user) => user.id === selectedId.value).roles;
		const rolesToAdd =
			selectedRoles.value.filter(
				({ name }) => !existingRoles.map((role) => role.name).includes(name)
			) ?? [];
		const rolesToRemove =
			existingRoles.filter(
				({ name }) => !selectedRoles.value.map((role) => role.name).includes(name)
			) ?? [];
		rolesToAdd.forEach((role) => {
			console.log(`add ${role.name}`);
			addRole(role);
		});
		rolesToRemove.forEach((role) => {
			console.log(`remove ${role.name}`);
			removeRole(role);
		});
	}
};

watch(
	() => view.value,
	() => {
		if (view.value === View.USER) {
			getRoles();
			getUsers();
		} else if (view.value === View.GROUP) {
			getGroups();
		}
	}
);

onMounted(() => {
	getRoles();
	getUsers();
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

.p-datatable:deep(.p-datatable-tbody > tr),
.p-datatable:deep(.p-datatable-thead > tr) {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
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

.p-multiselect {
	height: 2.5rem;
}

.p-multiselect:deep(.p-multiselect-label) {
	padding: 0.5rem;
}
</style>
