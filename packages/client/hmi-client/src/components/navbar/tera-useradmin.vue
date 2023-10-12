<template>
	<header>
		<h4>User Administration</h4>
		<Button
			class="p-button-rounded"
			icon="pi pi-times"
			@click="isUserAdministrationModalVisible = !isUserAdministrationModalVisible"
		/>
	</header>
	<DataTable
		:value="adminTableData"
		v-model:selection="selectedAdminRow"
		selectionMode="single"
		dataKey="id"
		@rowSelect="onAdminRowSelect"
		stripedRows
		responsiveLayout="scroll"
	>
		<Column field="email" header="EMail"></Column>
		<Column field="firstName" header="First Name"></Column>
		<Column field="lastName" header="First Name"></Column>
		<Column header="Roles">
			<template #body="slotProps">
				{{ getRoleNames(slotProps.data.roles) }}
			</template>
		</Column>
	</DataTable>
	<div>
		<Dropdown
			v-model="selectedRole"
			:options="systemRoles"
			optionLabel="name"
			optionValue="name"
			placeholder="Select a Role"
		/>

		<Button label="Add Role" class="p-button-secondary" @click="addRole" />
		<Button label="Remove Role" class="p-button-secondary" @click="removeRole" />
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Ref } from 'vue';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dropdown from 'primevue/dropdown';
import API from '@/api/api';

const isUserAdministrationModalVisible = ref(false);

interface Role {
	name: string;
}

let systemRoles: Ref<Object[]> = ref([]);
const adminTableData = ref();
const selectedRole = ref();
const selectedId = ref();
const selectedAdminRow = ref();

const getRoles = async () => {
	systemRoles = ref([]);
	try {
		const response = await API.get('/roles');
		if (response.status >= 200 && response.status < 300) {
			response.data.forEach((role: Role) => {
				systemRoles.value.push({ name: role.name });
			});
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

const onAdminRowSelect = (event: { originalEvent: MouseEvent; data: any; index: number }) => {
	selectedId.value = event.data.id;
};
const addRole = async () => {
	if (selectedId.value && selectedRole.value) {
		try {
			const response = await API({
				url: `/users/${selectedId.value}/roles/${selectedRole.value}`,
				method: 'POST',
				validateStatus: (status: number) => status < 400 //
			});
			if (response.status >= 200 && response.status < 300) {
				await getUsers();
			}
		} catch (err) {
			console.log(err);
		}
	}
	return null;
};
const removeRole = async () => {
	if (selectedId.value && selectedRole.value) {
		try {
			const response = await API({
				url: `/users/${selectedId.value}/roles/${selectedRole.value}`,
				method: 'DELETE',
				validateStatus: (status: number) => status < 400 //
			});
			if (response.status >= 200 && response.status < 300) {
				getUsers();
			}
		} catch (err) {
			console.log(err);
		}
	}
	return null;
};
const getRoleNames = (roles: [Role]) => {
	let names = '';
	if (roles) {
		roles.forEach((role: Role) => {
			if (names.length > 0) {
				names += ', ';
			}
			names += role.name;
		});
	}
	return names;
};

getRoles();
getUsers();
</script>

<style scoped></style>
