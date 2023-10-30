import API from '@/api/api';
import { PermissionRole } from '@/types/Types';

export async function getRoles(): Promise<PermissionRole[] | null> {
	const response = await API.get(`/roles`);
	return response?.data ?? null;
}
