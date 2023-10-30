import API from '@/api/api';
import { PermissionGroup } from '@/types/Types';

export async function getAllGroups(): Promise<PermissionGroup[] | null> {
	const response = await API.get(`/groups`);
	return response?.data ?? null;
}

export async function getGroup(id: string): Promise<PermissionGroup | null> {
	const response = await API.get(`/groups/${id}`);
	return response?.data ?? null;
}

export async function addGroupUserPermissions(
	groupId: string,
	userId: string,
	relationship: string
) {
	const response = await API.post(`/groups/${groupId}/permissions/user/${userId}/${relationship}`);
	if (response.status === 200) {
		return true;
	}
	return false;
}

export async function updateGroupUserPermissions(
	groupId: string,
	userId: string,
	oldRelationship: string,
	newRelationship: string
) {
	const response = await API.put(
		`/groups/${groupId}/permissions/user/${userId}/${oldRelationship}?to=${newRelationship}`
	);
	if (response.status === 200) {
		return true;
	}
	return false;
}

export async function removeGroupUserPermissions(
	groupId: string,
	userId: string,
	relationship: string
) {
	const response = await API.delete(
		`/groups/${groupId}/permissions/user/${userId}/${relationship}`
	);
	if (response.status === 200) {
		return true;
	}
	return false;
}
