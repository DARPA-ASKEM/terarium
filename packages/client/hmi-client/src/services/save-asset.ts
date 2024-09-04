import { ref } from 'vue';
import { createWorkflow } from '@/services/workflow';
import { updateCodeAsset, uploadCodeToProject } from '@/services/code';
import { createModel, updateModel } from '@/services/model';
import { useProjects } from '@/composables/project';
import { AssetType, ProjectAsset } from '@/types/Types';
import { logger } from '@/utils/logger';
import router from '@/router';
import { RouteName } from '@/router/routes';
import type { Model, Code } from '@/types/Types';
import type { Workflow } from '@/types/workflow';
import { activeProjectId } from '@/composables/activeProject';
import { getProjectIdFromUrl } from '@/api/api';

export type AssetToSave = Model | Workflow | File;

// TODO: Once assets have a type property, we can remove the assetType parameter

// Saves the asset as a new asset
export async function saveAs(
	newAsset: AssetToSave,
	assetType: AssetType,
	openOnSave: boolean = false,
	onSaveFunction?: Function
) {
	const projectId = activeProjectId.value || getProjectIdFromUrl();
	let response: ProjectAsset | null = null;

	switch (assetType) {
		case AssetType.Model:
			response = await createModel(newAsset as Model);
			break;
		case AssetType.Workflow:
			response = await createWorkflow(newAsset as Workflow);
			break;
		case AssetType.Code:
			response = await uploadCodeToProject(newAsset as File, ref(0));
			break;
		default:
			logger.info(`Saving for ${assetType} is not implemented.`);
			return;
	}

	if (!response?.id) {
		logger.info(`Failed to save ${assetType}.`);
		return;
	}

	// After saving notify the user and do any necessary actions
	logger.info(`${response.name} saved successfully in project ${useProjects().activeProject.value?.name}.`);
	if (openOnSave) {
		router.push({
			name: RouteName.Project,
			params: {
				projectId,
				pageType: assetType,
				assetId: response.assetId
			}
		});
	}

	if (onSaveFunction) onSaveFunction(response);
}

// Overwrites/updates the asset
export async function update(newAsset: AssetToSave, assetType: AssetType, onSaveFunction?: Function) {
	if (!(newAsset instanceof File) && !newAsset.id) {
		logger.error(`Can't update an asset that lacks an id.`);
		return;
	}

	let response: any = null;

	switch (assetType) {
		case AssetType.Model:
			response = await updateModel(newAsset as Model);
			break;
		case AssetType.Code:
			response = await updateCodeAsset(newAsset as Code);
			break;
		default:
			logger.info(`Update for ${assetType} is not implemented.`);
			return;
	}

	if (!response) {
		logger.error(`Failed to update ${assetType}.`);
		return;
	}

	// TODO: Consider calling this refresh within the update functions in the services themselves
	useProjects().refresh();

	logger.info(`Updated ${response.name}.`);
	if (onSaveFunction) onSaveFunction(response.name);
}
