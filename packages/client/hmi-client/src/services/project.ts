/**
 * Project
 */

import API from '@/api/api';
import { Project } from '@/types/Project';

// Define an empty Project
const empty = {
	id: '',
	name: 'Empty project',
	description: 'This is an empty project, used as placeholder',
	timestamp: ''
} as Project;

/**
 * Get a project per id
 * @return Project - the appropriate project, or an empty one if none returned by API
 */
async function get(projectId: string): Promise<Project> {
	const response = await API.get(`/projects/${projectId}`);
	return response?.data ?? empty;
}

export { empty, get };
