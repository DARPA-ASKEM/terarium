import { Model } from '@/types/Model';

// This is temp as it will be replaced by types/Types when this is more finalized and moved to hmi-server
export interface ModelConfig {
	id?: string;
	model: Model;
	initialValues: { [key: string]: number };
	parameterValues: { [key: string]: number };
}
