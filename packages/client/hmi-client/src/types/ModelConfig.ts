import { Model } from '@/types/Model';

// This is temp as it will be replaced by types/Types when this is more finalized and moved to hmi-server

// TDS should probably just store the model id...
export interface ModelConfigDB {
	id: string;
	initialValues: { [key: string]: number };
	parameterValues: { [key: string]: number };
}

// On the frontend its more convenient to use this to pass data through nodes
// Otherwise you'd have to getModel(modelId) in every node
export interface ModelConfig {
	model: Model;
	initialValues: { [key: string]: number };
	parameterValues: { [key: string]: number };
}
