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

export interface GithubFile {
    type: FileType;
    encoding: string;
    size: number;
    name: string;
    path: string;
    content: string;
    sha: string;
    url: string;
    gitUrl: string;
    htmlUrl: string;
    downloadUrl: string;
    links: Links;
    submoduleGitUrl: string;
    target: string;
    fileCategory: FileCategory;
}

export interface GithubRepo {
    files: { [P in FileCategory]?: GithubFile[] };
    totalFiles: number;
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
    framework: string;
    name: string;
    description: string;
    model_version: string;
    schema: string;
    model: { [index: string]: any };
    semantics?: ModelSemantics;
    metadata: ModelMetadata;
}

export interface ModelConfiguration {
    id: string;
    name: string;
    description?: string;
    modelId: string;
    configuration: any;
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
    modelId: string;
}

export interface Dataset {
    id?: string;
    timestamp?: any;
    username?: string;
    name: string;
    description?: string;
    dataSourceDate?: string;
    fileNames?: string[];
    url?: string;
    columns?: DatasetColumn[];
    metadata?: any;
    source?: string;
    grounding?: Grounding;
}

export interface DatasetColumn {
    name: string;
    dataType: ColumnType;
    formatStr?: string;
    annotations: { [index: string]: string[] };
    metadata?: { [index: string]: any };
    grounding?: { [index: string]: Grounding };
}

export interface Grounding {
    identifiers: { [index: string]: string };
    context?: { [index: string]: any };
}

export interface PresignedURL {
    url: string;
    method: string;
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

export interface Links {
    html: string;
    git: string;
    self: string;
}

export interface ModelSemantics {
    ode: OdeSemantics;
    typing?: TypingSemantics;
}

export interface ModelMetadata {
    processed_at: number;
    processed_by: string;
    variable_statements: VariableStatement[];
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
    description?: string;
    value?: number;
    grounding?: ModelGrounding;
    distribution?: ModelDistribution;
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

export interface OdeSemantics {
    rates: Rate[];
    initials?: any[];
    parameters?: ModelParameter[];
}

export interface TypingSemantics {
    type_system: TypeSystem;
    type_map: string[][];
}

export interface VariableStatement {
    id: string;
    variable: Variable;
    value?: StatementValue;
    metadata?: VariableStatementMetadata[];
    provenance?: ProvenanceInfo;
}

export interface ModelGrounding {
    identifiers: { [index: string]: any };
    context?: { [index: string]: any };
}

export interface ModelExpression {
    expression: string;
    expressionMathml: string;
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

export interface Rate {
    target: string;
    expression: string;
    expressionMathml: string;
}

export interface TypeSystem {
    states: State[];
    transitions: Transition[];
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
    dkg_grounding?: DKGConcept;
}

export interface VariableStatementMetadata {
    type: string;
    value: string;
}

export interface ProvenanceInfo {
    method: string;
    description: string;
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

export interface State {
    id: string;
    name?: string;
    description?: string;
    grounding?: ModelGrounding;
}

export interface Transition {
    id: string;
    input: string[];
    output: string[];
    grounding?: ModelGrounding;
    properties?: Properties;
}

export interface VariableMetadata {
    type: string;
    value: string;
}

export interface DataColumn {
    id: string;
    name: string;
    dataset: MetadataDataset;
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

export interface Properties {
    name: string;
    grounding?: ModelGrounding;
    description?: string;
}

export interface MetadataDataset {
    id: string;
    name: string;
    metadata: string;
}

export enum EventType {
    Search = "SEARCH",
}

export enum FileType {
    File = "file",
    Dir = "dir",
    Symlink = "symlink",
    Submodule = "submodule",
}

export enum FileCategory {
    Directory = "Directory",
    Code = "Code",
    Data = "Data",
    Documents = "Documents",
    Other = "Other",
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

export enum ColumnType {
    Unknown = "UNKNOWN",
    Boolean = "BOOLEAN",
    String = "STRING",
    Char = "CHAR",
    Integer = "INTEGER",
    Int = "INT",
    Float = "FLOAT",
    Double = "DOUBLE",
    Timestamp = "TIMESTAMP",
    Datetime = "DATETIME",
    Date = "DATE",
    Time = "TIME",
}
