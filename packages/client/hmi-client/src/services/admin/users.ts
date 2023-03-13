/**
 * Admin Users
 */

import AdminAPI from '@/api/adminapi';
import { logger } from '@/utils/logger';

/**
 * Get list of available roles
 * @return string[] | null
 */
async function getListRoles(): Promise<string[] | null> {
	try {
		const { status, data } = await AdminAPI.get('/roles');
		if (status >= 200 && status < 300) {
			return data.map((role) => role.name) as string[];
		}
		return null;
	} catch (error) {
		logger.error(`[ADMIN] ${error}`);
		return null;
	}
}

/**
 * Add Role to a User
 * @param id User Id
 * @param role Role name
 * @return Boolean
 */
async function addRole(id: string, role: string): Promise<boolean> {
	try {
		const { status } = await AdminAPI.post(`/user/${id}/roles?name=${role}`);
		return status >= 200 && status < 300;
	} catch (error) {
		logger.error(`[ADMIN] ${error}`);
		return false;
	}
}

/**
 * Remove Role to a User
 * @param id User Id
 * @param role Role name
 * @return Boolean
 */
async function removeRole(id: string, role: string): Promise<boolean> {
	try {
		const { status } = await AdminAPI.delete(`/user/${id}/roles?name=${role}`);
		return status >= 200 && status < 300;
	} catch (error) {
		logger.error(`[ADMIN] ${error}`);
		return false;
	}
}

export default { addRole, getListRoles, removeRole };
