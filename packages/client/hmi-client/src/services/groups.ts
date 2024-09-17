import API from '@/api/api';
import type { PermissionGroup } from '@/types/Types';
import { logger } from '@/utils/logger';

export async function getAllGroups(): Promise<PermissionGroup[] | null> {
	try {
		const response = await API.get(`/groups`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGroup(id: string): Promise<PermissionGroup | null> {
	try {
		const response = await API.get(`/groups/${id}`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function addGroupUserPermissions(groupId: string, userId: string, relationship: string) {
	try {
		const response = await API.post(`/groups/${groupId}/permissions/user/${userId}/${relationship}`);
		return response.status === 200;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

export async function updateGroupUserPermissions(
	groupId: string,
	userId: string,
	oldRelationship: string,
	newRelationship: string
) {
	try {
		const response = await API.put(
			`/groups/${groupId}/permissions/user/${userId}/${oldRelationship}?to=${newRelationship}`
		);
		return response.status === 200;
	} catch (error) {
		logger.error(error);
		return false;
	}
}

export async function removeGroupUserPermissions(groupId: string, userId: string, relationship: string) {
	try {
		const response = await API.delete(`/groups/${groupId}/permissions/user/${userId}/${relationship}`);
		return response.status === 200;
	} catch (error) {
		logger.error(error);
		return false;
	}
}
