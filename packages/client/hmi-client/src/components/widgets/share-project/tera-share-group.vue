<template>
	<Dialog
		v-model:visible="visible"
		modal
		:closable="false"
		header="Edit group"
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
				class="w-full sm"
			/>
			<section class="selected-users" v-if="selectedUsers.length > 0">
				<h6>People and groups with access</h6>
				<ul>
					<li v-for="(user, i) in selectedUsers" :key="i">
						<tera-user-card :user="user" />
					</li>
				</ul>
			</section>
		</section>
		<template #footer>
			<Button label="Cancel" class="p-button-secondary xsm" @click="visible = false" />
			<Button label="Done" class="xsm" @click="addSelectedUsersToGroup" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import { watch, ref, computed } from 'vue';
import Button from 'primevue/button';
import { getUsers } from '@/services/user';
// import { addGroupUserPermissions } from '@/services/groups';
import { PermissionUser } from '@/types/Types';
import TeraUserCard from './tera-user-card.vue';

const props = defineProps<{ modelValue: boolean; groupId: string }>();

const visible = ref(props.modelValue);
const users = ref<PermissionUser[]>([]);
const selectedUser = ref<PermissionUser | null>();
const selectedUsers = ref<PermissionUser[]>([]);
const usersMenu = computed(() =>
	users.value.map((u) => ({ id: u.id, name: u.firstName.concat(' ').concat(u.lastName) }))
);

function addNewSelectedUser(id: string) {
	const user = users.value.find((u) => u.id === id);
	if (!user) {
		return;
	}
	selectedUsers.value.push(user);
}

async function addSelectedUsersToGroup() {
	// await Promise.all(selectedUsers.value.map((user) =>
	// 	addGroupUserPermissions(props.groupId, user.id, '')
	// )).then(() => visible.value = false);
}

function onAfterHide() {
	selectedUser.value = null;
}

watch(
	() => visible.value,
	async () => {
		if (visible.value) {
			users.value = (await getUsers()) ?? [];
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

.p-dropdown.sm {
	margin-bottom: 0.5rem;
}
</style>
