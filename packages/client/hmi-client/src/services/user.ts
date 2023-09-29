import API from '@/api/api';
import { PermissionUser } from '@/types/Types';

export async function getUsers(): Promise<PermissionUser[] | null> {
	const response = await API.get(`/users`);
	return response?.data ?? null;
}
