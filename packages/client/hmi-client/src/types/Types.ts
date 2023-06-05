/* tslint:disable */
/* eslint-disable */

export interface Event {
    id?: string;
    timestampMillis?: number;
    projectId?: number;
    username?: string;
    type: EventType;
    value?: string;
}

export interface CsvAsset {
    csv: string[][];
    stats?: CsvColumnStats[];
    headers: string[];
}

export interface CsvColumnStats {
    bins: number[];
    minValue: number;
    maxValue: number;
    mean: number;
    median: number;
    sd: number;
}

export interface DocumentAsset {
    id?: number;
    title: string;
    xdd_uri: string;
}

export interface Model {
    id: string;
    name: string;
    description: string;
    schema: string;
    model: { [index: string]: any };
    properties: { [index: string]: any };
    metadata: ModelMetadata;
    content: ModelContent;
}

export interface ProvenanceQueryParam {
    rootId?: number;
    rootType?: ProvenanceType;
    userId?: number;
}

export interface Simulation {
    id?: number;
    name: string;
    description?: string;
    simulationParams: SimulationParams;
    result?: string;
    modelId: number;
}

export interface PetriNetModel {
    states: PetriNetState[];
    transitions: PetriNetTransition[];
    parameters?: ModelParameter[];
    metadata?: ModelMetadata;
}

export interface CalibrationParams {
    model: string;
    initials: { [index: string]: number };
    params: { [index: string]: number };
    timesteps_column: string;
    feature_mappings: { [index: string]: string };
    dataset: string;
}

export interface DocumentsResponseOK extends XDDResponseOK {
    data: Document[];
    nextPage: string;
    scrollId: string;
    hits: number;
    facets: { [index: string]: XDDFacetsItemResponse };
}

export interface ModelMetadata {
    processed_at: number;
    processed_by: string;
    variable_statements: VariableStatement[];
}

export interface ModelContent {
    metadata: any;
    I: { [index: string]: number }[];
    O: { [index: string]: number }[];
    S: Species[];
    T: { [index: string]: string }[];
}

export interface SimulationParams {
    model: string;
    initials: { [index: string]: number };
    tspan: number[];
    params: { [index: string]: number };
}

export interface PetriNetState {
    id: string;
    name: string;
    grounding: ModelGrounding;
    initial: ModelExpression;
}

export interface PetriNetTransition {
    id: string;
    input: string[];
    output: string[];
    grounding: ModelGrounding;
    properties: PetriNetTransitionProperties;
}

export interface ModelParameter {
    id: string;
    description: string;
    value: number;
    grounding: ModelGrounding;
    distribution: ModelDistribution;
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
    githubUrls: string[];
    knownTerms: { [index: string]: string[] };
    highlight: string[];
    relatedDocuments: Document[];
    relatedExtractions: Extraction[];
    knownEntities: KnownEntities;
    citationList: { [index: string]: string }[];
    citedBy: { [index: string]: any }[];
    abstract: string;
}

export interface XDDFacetsItemResponse {
    buckets: XDDFacetBucket[];
    doc_count_error_upper_bound: number;
    sum_other_doc_count: number;
}

export interface XDDResponseOK {
    v: number;
    license: string;
}

export interface VariableStatement {
    id: string;
    variable: Variable;
    value: StatementValue;
    metadata: VariableStatementMetadata[];
    provenance: ProvenanceInfo;
}

export interface Species {
    sname: string;
    miraIds: Ontology[];
    miraContext: Ontology[];
}

export interface ModelGrounding {
    identifiers: { [index: string]: any };
    context: { [index: string]: any };
}

export interface ModelExpression {
    expression: string;
    expression_mathml: string;
}

export interface PetriNetTransitionProperties {
    name: string;
    grounding: ModelGrounding;
    rate: ModelExpression;
}

export interface ModelDistribution {
    type: string;
    parameters: { [index: string]: any };
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

export interface KnownEntities {
    urlExtractions: XDDUrlExtraction[];
    summaries: { [index: string]: { [index: string]: string } };
}

export interface XDDFacetBucket {
    key: string;
    docCount: string;
}

export interface Variable {
    id: string;
    name: string;
    metadata: VariableMetadata[];
    column: DataColumn[];
    paper: Paper;
    equations: Equation[];
    dkg_groundings: DKGConcept[];
}

export interface StatementValue {
    value: string;
    type: string;
    dkg_grounding: DKGConcept;
}

export interface VariableStatementMetadata {
    type: string;
    value: string;
}

export interface ProvenanceInfo {
    method: string;
    description: string;
}

export interface Ontology {
    name: string;
    curie: string;
    title: string;
    description: string;
    link: string;
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

export interface XDDUrlExtraction {
    url: string;
    resourceTitle: string;
    extractedFrom: string[];
}

export interface VariableMetadata {
    type: string;
    value: string;
}

export interface DataColumn {
    id: string;
    name: string;
    dataset: Dataset;
}

export interface Paper {
    id: string;
    doi: string;
    file_directory: string;
}

export interface Equation {
    id: string;
    text: string;
    image: string;
}

export interface DKGConcept {
    id: string;
    name: string;
    score: number;
}

export interface Dataset {
    id: string;
    name: string;
    metadata: string;
}

export enum EventType {
    Search = "SEARCH",
}

export enum ProvenanceType {
    Dataset = "Dataset",
    Intermediate = "Intermediate",
    Model = "Model",
    ModelParameter = "ModelParameter",
    ModelRevision = "ModelRevision",
    Plan = "Plan",
    PlanParameter = "PlanParameter",
    Publication = "Publication",
    Project = "Project",
    Concept = "Concept",
    SimulationRun = "SimulationRun",
}
