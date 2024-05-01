import { ref } from 'vue';
import { createWorkflow } from '@/services/workflow';
import { uploadCodeToProject } from '@/services/code';
import { createModel, updateModel } from '@/services/model';
import { useProjects } from '@/composables/project';
import { AssetType } from '@/types/Types';
import { logger } from '@/utils/logger';
import router from '@/router';
import { RouteName } from '@/router/routes';

const projectResource = useProjects();

// TODO: Once assets have a type property, we can remove the assetType parameter

// Saves the asset as a new asset
export async function saveAs(
	newAsset: any,
	assetType: AssetType,
	openOnSave: boolean = false,
	onSaveFunction?: Function
) {
	let response: any = null;

	switch (assetType) {
		case AssetType.Model:
			response = await createModel(newAsset);
			break;
		case AssetType.Workflow:
			response = await createWorkflow(newAsset);
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

	const projectId = projectResource.activeProject.value?.id;
	if (!projectId) {
		logger.error(`Asset can't be saved since target project doesn't exist.`);
		return;
	}
	await projectResource.addAsset(assetType, response.id, projectId);

	// After saving notify the user and do any necessary actions
	logger.info(
		`${response.name} saved successfully in project ${projectResource.activeProject.value?.name}.`
	);
	if (openOnSave) {
		router.push({
			name: RouteName.Project,
			params: {
				projectId,
				pageType: assetType,
				assetId: response.id
			}
		});
	}

	if (onSaveFunction) onSaveFunction(response);
}

// Overwrites/updates the asset
export async function overwrite(newAsset: any, assetType: AssetType, onSaveFunction?: Function) {
	let response: any = null;

	switch (assetType) {
		case AssetType.Model:
			response = await updateModel(newAsset);
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
	projectResource.refresh();

	logger.info(`Updated ${response.name}.`);
	if (onSaveFunction) onSaveFunction(response.name);
}
