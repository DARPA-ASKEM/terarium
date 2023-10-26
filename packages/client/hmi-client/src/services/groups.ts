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
	const response = await API.post(`/${groupId}/permissions/user/${userId}/${relationship}`);
	console.log(response);
	return response?.data ?? null;
}
