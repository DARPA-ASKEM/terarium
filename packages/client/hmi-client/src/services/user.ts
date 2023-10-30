import API from '@/api/api';
import { PermissionRole, PermissionUser } from '@/types/Types';

export async function getUsers(): Promise<PermissionUser[] | null> {
	const response = await API.get(`/users`);
	return response?.data ?? null;
}

export async function addRole(userId: string, roleName: string): Promise<PermissionRole | null> {
	const response = await API.post(`/users/${userId}/roles/${roleName}`);
	return response?.data ?? null;
}

export async function removeRole(userId: string, roleName: string): Promise<PermissionRole | null> {
	const response = await API.delete(`/users/${userId}/roles/${roleName}`);
	return response?.data ?? null;
}
