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
