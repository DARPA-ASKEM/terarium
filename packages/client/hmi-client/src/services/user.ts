import API from '@/api/api';
import type { PermissionRole, PermissionUser } from '@/types/Types';
import { logger } from '@/utils/logger';

export async function getUsers(): Promise<PermissionUser[] | null> {
	try {
		const response = await API.get(`/users`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function addRole(userId: string, roleName: string): Promise<PermissionRole | null> {
	try {
		const response = await API.post(`/users/${userId}/roles/${roleName}`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function removeRole(userId: string, roleName: string): Promise<PermissionRole | null> {
	try {
		const response = await API.delete(`/users/${userId}/roles/${roleName}`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
