import { Model } from '@/types/Model';

// This is temp as it will be replaced by types/Types when this is more finalized and moved to hmi-server

// TDS should probably just store the model id...
export interface ModelConfigDB {
	id: string;
	initialValues: { [key: string]: number };
	parameterValues: { [key: string]: number };
}

export interface ModelConfig {
	model: Model;
	initialValues: { [key: string]: number };
	parameterValues: { [key: string]: number };
}
