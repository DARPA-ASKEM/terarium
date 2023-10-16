<template>
	<main>
		<header>
			<h4>User Administration</h4>
		</header>
		<DataTable
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
import { ref, onMounted } from 'vue';
import DataTable, { DataTableRowSelectEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import API from '@/api/api';
import MultiSelect from 'primevue/multiselect';

interface Role {
	id: string;
	name: string;
}

const systemRoles = ref<Role[]>([]);
const adminTableData = ref();
const selectedId = ref();
const selectedAdminRow = ref();
const selectedRoles = ref<Role[]>([]);

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

const onRowSelect = (event: DataTableRowSelectEvent) => {
	selectedId.value = event.data.id;
	selectedRoles.value = event.data.roles;
	console.log('rowselect');
};

const addRole = async (role) => {
	try {
		const response = await API({
			url: `/users/${selectedId.value}/roles/${role.id}`,
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
			url: `/users/${selectedId.value}/roles/${role.id}`,
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
		console.log(existingRoles);
		console.log(selectedRoles.value);
		const rolesToAdd =
			selectedRoles.value.filter(
				({ name }) => !existingRoles.map((role) => role.name).includes(name)
			) ?? [];
		const rolesToRemove =
			existingRoles.filter(
				({ name }) => !selectedRoles.value.map((role) => role.name).includes(name)
			) ?? [];
		console.log(rolesToAdd);
		console.log(rolesToRemove);
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
