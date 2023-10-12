import API from '@/api/api';
import { User } from '@/types/Types';

export async function getUsers(): Promise<User[] | null> {
	const response = await API.get(`/users`);
	return response?.data ?? null;
}
