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

export interface EvaluationScenarioSummary {
    name: string;
    username: string;
    task: string;
    description: string;
    notes: string;
    timestampMillis: number;
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

export interface Artifact {
    id?: string;
    timestamp?: any;
    username: string;
    name: string;
    description?: string;
    fileNames: string[];
    metadata?: any;
    concepts?: Concept[];
}

export interface CsvAsset {
    csv: string[][];
    stats?: CsvColumnStats[];
    headers: string[];
    rowCount: number;
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
    model_version?: string;
    schema: string;
    schema_name?: string;
    model: { [index: string]: any };
    properties?: any;
    semantics?: ModelSemantics;
    metadata?: any;
}

export interface ModelConfiguration {
    id: string;
    name: string;
    description?: string;
    modelId: string;
    configuration: any;
}

export interface Project {
    name: string;
    description?: string;
    timestamp?: Date;
    active: boolean;
    concept?: Concept;
    assets?: Assets;
    metadata?: { [index: string]: string };
    username: string;
    id?: string;
    relatedDocuments?: Document[];
}

export interface ProvenanceQueryParam {
    rootId?: number;
    rootType?: ProvenanceType;
    userId?: number;
}

export interface Simulation {
    id: string;
    executionPayload: any;
    name?: string;
    description?: string;
    resultFiles?: string[];
    type: string;
    status: string;
    startTime?: string;
    completedTime?: string;
    engine: string;
    workflowId: string;
    userId?: number;
    projectId?: number;
}

export interface Dataset {
    id?: string;
    timestamp?: any;
    username?: string;
    name: string;
    description?: string;
    dataSourceDate?: string;
    fileNames?: string[];
    datasetUrl?: string;
    columns?: DatasetColumn[];
    metadata?: any;
    source?: string;
    grounding?: Grounding;
}

export interface DatasetColumn {
    name: string;
    dataType: ColumnType;
    formatStr?: string;
    annotations: string[];
    metadata?: { [index: string]: any };
    grounding?: Grounding;
    description?: string;
}

export interface Grounding {
    identifiers: { [index: string]: string };
    context?: { [index: string]: any };
}

export interface PresignedURL {
    url: string;
    method: string;
}

export interface State {
    id: string;
    name?: string;
    description?: string;
    grounding?: ModelGrounding;
    units?: ModelUnit;
}

export interface Transition {
    id: string;
    input: string[];
    output: string[];
    grounding?: ModelGrounding;
    properties?: Properties;
}

export interface TypeSystem {
    states: State[];
    transitions: Transition[];
}

export interface TypeSystemExtended {
    name: string;
    description: string;
    schema: string;
    model_version: string;
    model: { [index: string]: any };
    properties?: any;
    semantics?: ModelSemantics;
    metadata?: any;
}

export interface TypingSemantics {
    map: string[][];
    system: any;
}

export interface PetriNetModel {
    states: PetriNetState[];
    transitions: PetriNetTransition[];
}

export interface ExtractionResponse {
    id: string;
    created_at: Date;
    enqueued_at: Date;
    started_at: Date;
    status: string;
    extraction_error: string;
    result: any;
}

export interface DKG {
    curie: string;
    name: string;
    description: string;
    link: string;
}

export interface CalibrationRequestCiemss {
    modelConfigId: string;
    extra: any;
    timespan?: TimeSpan;
    dataset: DatasetLocation;
    engine: string;
}

export interface CalibrationRequestJulia {
    modelConfigId: string;
    extra: any;
    timespan?: TimeSpan;
    dataset: DatasetLocation;
    engine: string;
}

export interface DatasetLocation {
    id: string;
    filename: string;
    mappings?: any;
}

export interface EnsembleCalibrationCiemssRequest {
    modelConfigs: EnsembleModelConfigs[];
    dataset: DatasetLocation;
    timespan: TimeSpan;
    extra: any;
    engine: string;
}

export interface EnsembleModelConfigs {
    id: string;
    solutionMappings: { [index: string]: string };
    weight: number;
}

export interface EnsembleSimulationCiemssRequest {
    modelConfigs: EnsembleModelConfigs[];
    timespan: TimeSpan;
    extra: any;
    engine: string;
}

export interface SimulationRequest {
    modelConfigId: string;
    timespan: TimeSpan;
    extra: any;
    engine: string;
}

export interface TimeSpan {
    start: number;
    end: number;
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

export interface Concept {
    id: string;
    curie: string;
    type: Type;
    status: OntologicalField;
    object_id: string;
}

export interface ModelSemantics {
    ode: OdeSemantics;
    span?: any[];
    typing?: TypingSemantics;
}

export interface Assets {
    datasets: Dataset[];
    extractions: Extraction[];
    models: Model[];
    publications: DocumentAsset[];
    workflows: Workflow[];
    artifacts: Artifact[];
}

export interface Document {
    gddId: string;
    title: string;
    abstractText: string;
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
}

export interface ModelGrounding {
    identifiers: { [index: string]: any };
    context?: { [index: string]: any };
    modifiers?: any;
}

export interface ModelUnit {
    expression: string;
    expression_mathml: string;
}

export interface Properties {
    name: string;
    grounding?: ModelGrounding;
    description?: string;
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
    grounding?: ModelGrounding;
    properties: PetriNetTransitionProperties;
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
    initials?: Initial[];
    parameters?: ModelParameter[];
    observables?: Observable[];
    time?: any;
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

export interface Workflow {
    id: string;
    name: string;
    description: string;
    transform: any;
    nodes: any[];
    edges: any[];
}

export interface KnownEntities {
    urlExtractions: XDDUrlExtraction[];
    askemObjects: Extraction[];
    summaries: string[];
}

export interface ModelExpression {
    expression: string;
    expression_mathml: string;
}

export interface PetriNetTransitionProperties {
    name: string;
    description: string;
    grounding?: ModelGrounding;
}

export interface XDDFacetBucket {
    key: string;
    docCount: string;
}

export interface Rate {
    target: string;
    expression: string;
    expression_mathml?: string;
}

export interface Initial {
    target: string;
    expression: string;
    expression_mathml: string;
}

export interface ModelParameter {
    id: string;
    name?: string;
    description?: string;
    value?: number;
    grounding?: ModelGrounding;
    distribution?: ModelDistribution;
    unit?: ModelUnit;
}

export interface Observable {
    id: string;
    name?: string;
    states?: string[];
    expression?: string;
    expression_mathml?: string;
}

export interface ExtractionProperties {
    title: string;
    trustScore: string;
    abstractText: string;
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
}

export interface XDDUrlExtraction {
    url: string;
    resourceTitle: string;
    extractedFrom: string[];
}

export interface ModelDistribution {
    type: string;
    parameters: { [index: string]: any };
}

export enum EvaluationScenarioStatus {
    Started = "STARTED",
    Paused = "PAUSED",
    Resumed = "RESUMED",
    Stopped = "STOPPED",
}

export enum EventType {
    Search = "SEARCH",
    EvaluationScenario = "EVALUATION_SCENARIO",
    RouteTiming = "ROUTE_TIMING",
    ProxyTiming = "PROXY_TIMING",
    AddResourcesToProject = "ADD_RESOURCES_TO_PROJECT",
    ExtractModel = "EXTRACT_MODEL",
    PersistModel = "PERSIST_MODEL",
    TransformPrompt = "TRANSFORM_PROMPT",
    AddCodeCell = "ADD_CODE_CELL",
    RunSimulation = "RUN_SIMULATION",
    RunCalibrate = "RUN_CALIBRATE",
    GithubImport = "GITHUB_IMPORT",
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

export enum Type {
    Datasets = "DATASETS",
    Extractions = "EXTRACTIONS",
    Intermediates = "INTERMEDIATES",
    Models = "MODELS",
    Plans = "PLANS",
    Publications = "PUBLICATIONS",
    SimulationRuns = "SIMULATION_RUNS",
}

export enum OntologicalField {
    Object = "OBJECT",
    Unit = "UNIT",
}
