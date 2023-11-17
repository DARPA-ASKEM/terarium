import API from '@/api/api';
import { PermissionRole } from '@/types/Types';
import { logger } from '@/utils/logger';

export async function getRoles(): Promise<PermissionRole[] | null> {
	try {
		const response = await API.get(`/roles`);
		return response?.data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
