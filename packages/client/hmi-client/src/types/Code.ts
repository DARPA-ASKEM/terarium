export interface CodeArtifactExtractionMetaData {
	metadata: Metadata;
}
export interface Metadata {
	dynamics?: DynamicsEntity[] | null;
}
export interface DynamicsEntity {
	name?: string;
	filename: string;
	block: string;
}
