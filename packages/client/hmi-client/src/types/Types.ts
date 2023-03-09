/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.1.1185 on 2023-03-09 15:27:56.

export interface Annotation {
	id: string;
	timestampMillis: number;
	projectId: number;
	content: string;
	username: string;
	artifact_id: string;
	artifact_type: string;
}

export interface Event {
	id?: string;
	timestampMillis?: number;
	projectId?: number;
	username?: string;
	type: EventType;
	value?: string;
}

export interface CodeRequest {
	files: string[];
	blobs: string[];
	system_name: string;
	root_name: string;
}

export interface StoredModel {
	id: string;
	inputs: string;
	outputs: string;
}

export interface User {
	username: string;
	roles: string[];
}

export interface Assets {
	datasets: Dataset[];
	extractions: Extraction[];
	intermediates: Intermediate[];
	models: Model[];
	plans: SimulationPlan[];
	publications: Publication[];
	simulationRuns: SimulationRun[];
}

export interface Association {
	id: string;
	role: Role;
	person_id: string;
	resource_id: string;
	resource_type: Type;
}

export interface Concept {
	id: string;
	curie: string;
	type: Type;
	status: OntologicalField;
	object_id: string;
}

export interface Dataset {
	id: number;
	name: string;
	url: string;
	description: string;
	timestamp: Date;
	deprecated: boolean;
	sensitivity: string;
	quality: string;
	annotations: any;
	maintainer: string;
	temporal_resolution: string;
	geospatial_resolution: string;
	simulation_run: boolean;
}

export interface Feature {
	id: string;
	description: string;
	name: string;
	dataset_id: string;
	display_name: string;
	value_type: ValueType;
}

export interface Intermediate {
	id: string;
	timestamp: Date;
	source: IntermediateSource;
	type: IntermediateFormat;
	content: string;
}

export interface Model {
	id: number;
	name: string;
	description: string;
	framework: string;
	timestamp: Date;
	content: ModelContent;
	concept: Concept;
	parameters: { [index: string]: any };
}

export interface ModelContent {
	s: { [index: string]: string }[];
	o: { [index: string]: number }[];
	i: { [index: string]: number }[];
	t: { [index: string]: string }[];
}

export interface ModelFramework {
	name: string;
	version: string;
	semantics: string;
}

export interface OntologyConcept {
	id: string;
	curie: string;
	type: TaggableType;
	status: OntologicalField;
	object_id: number;
}

export interface Person {
	id: string;
	name: string;
	email: string;
	website: string;
	org: string;
	is_registered: boolean;
}

export interface Project {
	name: string;
	description: string;
	timestamp: Date;
	active: boolean;
	concept: Concept;
	assets: Assets;
	username: string;
	id: string;
	relatedDocuments: Document[];
}

export interface Provenance {
	id: string;
	timestamp: Date;
	left: string;
	right: string;
	relation_type: RelationType;
	left_type: Type;
	right_type: Type;
	user_id: string;
}

export interface ProvenanceQueryParameters {
	root_id: number;
	root_type: string;
	user_id: number;
}

export interface Publication {
	id: number;
	title: string;
	xdd_uri: string;
}

export interface Qualifier {
	id: string;
	description: string;
	name: string;
	dataset_id: string;
	value_type: ValueType;
}

export interface ResourceType {
	type: 'ResourceType';
	id: string;
}

export interface SimulationPlan {
	id: number;
	simulator: string;
	query: string;
	content: any;
	concept: Concept;
	model_id: string;
}

export interface SimulationRun {
	id: number;
	success: boolean;
	response: string;
	parameters: { [index: string]: string };
	simulator_id: string;
	timestamp: Date;
	completed_at: Date;
}

export interface SimulationRunDescription {
	id: string;
	success: boolean;
	response: string;
	simulator_id: string;
	timestamp: Date;
	completed_at: Date;
}

export interface Software {
	id: string;
	timestamp: Date;
	source: string;
	storage_uri: string;
}

export interface Dictionary {
	name: string;
	source: string;
	dict_id: number;
	base_classification: string;
	case_sensitive: boolean;
	last_updated: Date;
}

export interface Document {
	gddId: string;
	title: string;
	journal: string;
	type: string;
	number: string;
	pages: string;
	publisher: string;
	volume: string;
	year: string;
	link: { [index: string]: string }[];
	author: { [index: string]: string }[];
	identifier: { [index: string]: string }[];
	knownTerms: { [index: string]: string[] };
	highlight: string[];
	relatedDocuments: Document[];
	relatedExtractions: Extraction[];
	knownEntities: KnownEntities;
	citationList: { [index: string]: string }[];
	abstract: string;
}

export interface Extraction {
	id: number;
	askemClass: string;
	properties: ExtractionProperties;
	askemId: string;
	xddCreated: Date;
	xddRegistrant: number;
	highlight: string[];
}

export interface ExtractionProperties {
	title: string;
	trustScore: string;
	xddId: string;
	documentId: string;
	documentTitle: string;
	contentText: string;
	indexInDocument: number;
	contentJSON: any;
	image: string;
	relevantSentences: string;
	sectionID: string;
	sectionTitle: string;
	caption: string;
	documentBibjson: Document;
	doi: string;
	abstract: string;
}

export interface KnownEntities {
	urlExtractions: XDDUrlExtraction[];
	summaries: { [index: string]: { [index: string]: string } };
}

export interface RelatedDocument {
	bibjson: Document;
	score: number;
	document: Document;
}

export interface XDDUrlExtraction {
	url: string;
	resourceTitle: string;
	extractedFrom: string[];
}

export interface AutoComplete {
	suggest: Suggest;
	autoCompletes: string[];
}

export interface EntitySuggestFuzzy {
	options: { [index: string]: any }[];
}

export interface Suggest {
	entitySuggestFuzzy: EntitySuggestFuzzy[];
}

export interface Edge {
	source: string;
	target: string;
}

export interface Graph {
	nodes: Node[];
	edges: Edge[];
}

export interface ModelCompositionParams {
	modelA: PetriNet;
	modelB: PetriNet;
	statesToMerge: { [index: string]: string }[];
}

export interface Node {
	name: string;
	type: string;
}

export interface PetriNet {
	S: { [index: string]: string }[];
	T: { [index: string]: string }[];
	I: { [index: string]: number }[];
	O: { [index: string]: number }[];
}

export interface SimulateParams {
	variables: { [index: string]: number };
	parameters: { [index: string]: number };
}

export type ResourceTypeUnion = Publication | Model;

export enum EventType {
	Search = 'SEARCH'
}

export enum IntermediateFormat {
	Bilayer = 'BILAYER',
	Gromet = 'GROMET',
	Other = 'OTHER',
	Sbml = 'SBML'
}

export enum IntermediateSource {
	Mrepresentationa = 'MREPRESENTATIONA',
	Skema = 'SKEMA'
}

export enum OntologicalField {
	Object = 'OBJECT',
	Unit = 'UNIT'
}

export enum ProvenanceType {
	Dataset = 'DATASET',
	Intermediate = 'INTERMEDIATE',
	Model = 'MODEL',
	ModelParameter = 'MODEL_PARAMETER',
	ModelRevision = 'MODEL_REVISION',
	Plan = 'PLAN',
	PlanParameter = 'PLAN_PARAMETER',
	Publication = 'PUBLICATION',
	Project = 'PROJECT',
	Concept = 'CONCEPT',
	SimulationRun = 'SIMULATION_RUN'
}

export enum RelationType {
	Cites = 'CITES',
	CopiedFrom = 'COPIED_FROM',
	DerivedFrom = 'DERIVED_FROM',
	EditedFrom = 'EDITED_FROM',
	GluedFrom = 'GLUED_FROM',
	StratifiedFrom = 'STRATIFIED_FROM'
}

export enum Type {
	Datasets = 'DATASETS',
	Extractions = 'EXTRACTIONS',
	Intermediates = 'INTERMEDIATES',
	Models = 'MODELS',
	Plans = 'PLANS',
	Publications = 'PUBLICATIONS',
	SimulationRuns = 'SIMULATION_RUNS'
}

export enum Role {
	Author = 'AUTHOR',
	Contributor = 'CONTRIBUTOR',
	Maintainer = 'MAINTAINER',
	Other = 'OTHER'
}

export enum TaggableType {
	Datasets = 'DATASETS',
	Features = 'FEATURES',
	Intermediates = 'INTERMEDIATES',
	ModelParameters = 'MODEL_PARAMETERS',
	Models = 'MODELS',
	Projects = 'PROJECTS',
	Publications = 'PUBLICATIONS',
	Qualifiers = 'QUALIFIERS',
	SimulationParameters = 'SIMULATION_PARAMETERS',
	SimulationPlans = 'SIMULATION_PLANS',
	SimulationRuns = 'SIMULATION_RUNS'
}

export enum ValueType {
	Binary = 'BINARY',
	Bool = 'BOOL',
	Float = 'FLOAT',
	Int = 'INT',
	Str = 'STR'
}
