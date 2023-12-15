/* tslint:disable */
/* eslint-disable */

export interface ClientConfig {
    baseUrl: string;
    clientLogShippingEnabled: boolean;
    clientLogShippingIntervalMillis: number;
    sseHeartbeatIntervalMillis: number;
}

export interface Event {
    id?: string;
    timestampMillis?: number;
    projectId?: number;
    userId?: string;
    type: EventType;
    value?: string;
}

export interface ClientEvent<T> {
    id: string;
    createdAtMs: number;
    type: ClientEventType;
    data: T;
}

export interface ClientLog {
    level: string;
    timestampMillis: number;
    message: string;
    args?: string[];
}

export interface User {
    id: string;
    createdAtMs: number;
    lastLoginAtMs: number;
    roles: Role[];
    username: string;
    email: string;
    givenName: string;
    familyName: string;
    name: string;
    enabled: boolean;
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
    data: { [index: string]: string }[];
}

export interface CsvColumnStats {
    bins: number[];
    minValue: number;
    maxValue: number;
    mean: number;
    median: number;
    sd: number;
}

export interface ExternalPublication {
    id?: number;
    title: string;
    xdd_uri: string;
}

export interface Grounding {
    identifiers: { [index: string]: string };
    context?: { [index: string]: any };
}

export interface NotebookSession {
    id: string;
    name: string;
    description?: string;
    data: any;
    timestamp: string;
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
    publicProject?: boolean;
    userPermission?: string;
    id?: string;
    relatedDocuments?: Document[];
}

export interface ResponseId {
    id: string;
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

export interface Code {
    id?: string;
    timestamp?: Date;
    name: string;
    description: string;
    files?: { [index: string]: CodeFile };
    repoUrl?: string;
    metadata?: any;
}

export interface CodeFile {
    language: ProgrammingLanguage;
    dynamics: Dynamics;
}

export interface Dynamics {
    name: string;
    description: string;
    block: string[];
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

export interface AddDocumentAssetFromXDDRequest {
    document: Document;
    projectId: string;
}

export interface AddDocumentAssetFromXDDResponse {
    documentAssetId: string;
    pdfUploadError: boolean;
    extractionJobId: string;
}

export interface DocumentAsset {
    id?: string;
    name?: string;
    description?: string;
    timestamp?: string;
    username?: string;
    fileNames?: string[];
    documentUrl?: string;
    metadata?: { [index: string]: any };
    source?: string;
    text?: string;
    grounding?: Grounding;
    concepts?: Concept[];
    assets?: DocumentExtraction[];
}

export interface Equation {
    id?: string;
    timestamp?: Date;
    username?: string;
    name?: string;
    equationType: EquationType;
    content: string;
    metadata?: { [index: string]: any };
    source?: EquationSource;
}

export interface EquationSource {
    extractedFrom?: string;
    documentAssetName?: string;
    hmiGenerated?: boolean;
}

export interface Model {
    id: string;
    header: ModelHeader;
    model: { [index: string]: any };
    properties?: any;
    semantics?: ModelSemantics;
    metadata?: ModelMetadata;
}

export interface ModelConfiguration {
    id: string;
    name: string;
    description?: string;
    modelId: string;
    configuration: any;
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
    metadata?: ModelMetadata;
}

export interface TypingSemantics {
    map: string[][];
    system: any;
}

export interface DecapodesComponent {
    modelInterface: string[];
    model: DecapodesExpression;
    _type: string;
}

export interface DecapodesEquation {
    lhs: any;
    rhs: any;
    _type: string;
}

export interface DecapodesExpression {
    context: any[];
    equations: DecapodesEquation[];
    _type: string;
}

export interface DecapodesTerm {
    name?: string;
    var?: DecapodesTerm;
    symbol?: string;
    space?: string;
    fs?: string[];
    arg?: DecapodesTerm;
    f?: string;
    arg1?: DecapodesTerm;
    arg2?: DecapodesTerm;
    args?: DecapodesTerm[];
    _type: string;
}

export interface PetriNetModel {
    states: PetriNetState[];
    transitions: PetriNetTransition[];
}

export interface ProvenanceQueryParam {
    nodes?: boolean;
    types?: ProvenanceType[];
    hops?: number;
    limit?: number;
    verbose?: boolean;
    rootId?: string;
    rootType?: ProvenanceType;
    userId?: number;
}

export interface RegNetBaseProperties {
    name: string;
    grounding: ModelGrounding;
    rate_constant: any;
}

export interface RegNetEdge {
    source: string;
    target: string;
    id: string;
    sign: boolean;
    properties?: RegNetBaseProperties;
}

export interface RegNetModel {
    vertices: RegNetVertex[];
    edges: RegNetEdge[];
    parameters?: RegNetParameter[];
}

export interface RegNetParameter {
    id: string;
    description?: string;
    value?: number;
    grounding?: ModelGrounding;
    distribution?: ModelDistribution;
}

export interface RegNetVertex {
    id: string;
    name: string;
    sign: boolean;
    initial?: any;
    rate_constant?: any;
    grounding?: ModelGrounding;
}

export interface DocumentsResponseOK extends XDDResponseOK {
    data: Document[];
    nextPage: string;
    scrollId: string;
    hits: number;
    facets: { [index: string]: XDDFacetsItemResponse };
}

export interface EvaluationScenarioSummary {
    name: string;
    userId: string;
    task: string;
    description: string;
    notes: string;
    timestampMillis: number;
}

export interface ExtractionResponse {
    id: string;
    status: string;
    result: ExtractionResponseResult;
}

export interface ExtractionResponseResult {
    created_at: Date;
    enqueued_at: Date;
    started_at: Date;
    job_error: string;
    job_result: any;
}

export interface FunmanPostQueriesRequest {
    model: Model;
    request: FunmanWorkRequest;
}

export interface FunmanConfig {
    tolerance?: number;
    queueTimeout?: number;
    numberOfProcesses?: number;
    waitTimeout?: number;
    waitActionTimeout?: number;
    solver?: string;
    numSteps?: number;
    stepSize?: number;
    numInitialBoxes?: number;
    saveSmtlib?: boolean;
    drealPrecision?: number;
    drealLogLevel?: string;
    constraintNoise?: number;
    initialStateTolerance?: number;
    drealMcts?: boolean;
    substituteSubformulas?: boolean;
    use_compartmental_constraints?: boolean;
    normalize?: boolean;
    normalization_constant?: number;
}

export interface FunmanInterval {
    ub?: number;
    lb?: number;
    closed_upper_bound?: boolean;
}

export interface FunmanParameter {
    name: string;
    interval: FunmanInterval;
    label: string;
}

export interface FunmanWorkRequest {
    query?: any;
    constraints?: any;
    parameters?: FunmanParameter[];
    config?: FunmanConfig;
    structure_parameters?: any;
}

export interface Curies {
    sources: string[];
    targets: string[];
}

export interface DKG {
    curie: string;
    name: string;
    description: string;
    link: string;
}

export interface PermissionGroup {
    id: string;
    name: string;
    relationship?: string;
    permissionRelationships?: PermissionRelationships;
}

export interface PermissionProject {
    id: string;
    relationship: string;
}

export interface PermissionRelationships {
    permissionGroups: PermissionGroup[];
    permissionUsers: PermissionUser[];
    permissionProjects: PermissionProject[];
}

export interface PermissionUser {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    roles?: PermissionRole[];
    relationship?: string;
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

export interface EnsembleCalibrationCiemssRequest {
    modelConfigs: EnsembleModelConfigs[];
    dataset: DatasetLocation;
    timespan: TimeSpan;
    extra: any;
    engine: string;
}

export interface EnsembleSimulationCiemssRequest {
    modelConfigs: EnsembleModelConfigs[];
    timespan: TimeSpan;
    extra: any;
    engine: string;
}

export interface ScimlStatusUpdate {
    loss: number;
    iter: number;
    params: { [index: string]: number };
    id: string;
    solData: { [index: string]: any };
    timesteps: number[];
}

export interface SimulationRequest {
    modelConfigId: string;
    timespan: TimeSpan;
    extra: any;
    engine: string;
    interventions?: Intervention[];
}

export interface DatasetLocation {
    id: string;
    filename: string;
    mappings?: any;
}

export interface EnsembleModelConfigs {
    id: string;
    solutionMappings: { [index: string]: string };
    weight: number;
}

export interface Intervention {
    name: string;
    timestep: number;
    value: number;
}

export interface TimeSpan {
    start: number;
    end: number;
}

export interface UserEvent {
    type: EventType;
    user: UserOld;
    id: string;
    message: any;
}

export interface Role {
    id: number;
    name: string;
    authorities: AuthorityInstance[];
}

export interface Links {
    html: string;
    git: string;
    self: string;
}

export interface Concept {
    id: string;
    curie: string;
    type: AssetType;
    status: OntologicalField;
    object_id: string;
}

export interface Assets {
    datasets: Dataset[];
    extractions: Extraction[];
    models: Model[];
    publications: ExternalPublication[];
    workflows: Workflow[];
    artifacts: Artifact[];
    code: Code[];
    documents: DocumentAsset[];
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
    knownEntitiesCounts: KnownEntitiesCounts;
    citationList: { [index: string]: string }[];
    citedBy: { [index: string]: any }[];
}

export interface DocumentExtraction {
    fileName: string;
    assetType: ExtractionAssetType;
    metadata: { [index: string]: any };
}

export interface ModelHeader {
    name: string;
    schema: string;
    schema_name?: string;
    description: string;
    model_version?: string;
}

export interface ModelSemantics {
    ode: OdeSemantics;
    span?: any[];
    typing?: TypingSemantics;
}

/**
 * @deprecated
 */
export interface ModelMetadata {
    processed_at?: number;
    processed_by?: string;
    variable_statements?: VariableStatement[];
    annotations?: Annotations;
    attributes?: any[];
    timeseries?: { [index: string]: any };
    card?: Card;
    provenance?: string[];
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

export interface ModelDistribution {
    type: string;
    parameters: { [index: string]: any };
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

export interface PermissionRole {
    id: string;
    name: string;
    users: PermissionUser[];
}

export interface UserOld {
    username: string;
    roles: string[];
}

export interface AuthorityInstance {
    id: number;
    mask: number;
    authority: Authority;
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

export interface KnownEntitiesCounts {
    askemObjectCount: number;
    urlExtractionCount: number;
}

export interface OdeSemantics {
    rates: Rate[];
    initials?: Initial[];
    parameters?: ModelParameter[];
    observables?: Observable[];
    time?: any;
}

export interface VariableStatement {
    id: string;
    variable: Variable;
    value?: StatementValue;
    metadata?: VariableStatementMetadata[];
    provenance?: ProvenanceInfo;
}

export interface Annotations {
    license?: string;
    authors?: string[];
    references?: string[];
    time_scale?: string;
    time_start?: string;
    time_end?: string;
    locations?: string[];
    pathogens?: string[];
    diseases?: string[];
    hosts?: string[];
    model_types?: string[];
}

export interface Card {
    description?: string;
    authorInst?: string;
    authorAuthor?: string;
    authorEmail?: string;
    date?: string;
    schema?: string;
    provenance?: string;
    dataset?: string;
    complexity?: string;
    usage?: string;
    license?: string;
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

export interface Authority {
    id: number;
    name: string;
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

export interface Variable {
    id: string;
    name: string;
    metadata: VariableMetadata[];
    column: DataColumn[];
    paper: Paper;
    equations: EquationVariable[];
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

export interface EquationVariable {
    id: string;
    text: string;
    image: string;
}

export interface DKGConcept {
    id: string;
    name: string;
    score: number;
}

export interface MetadataDataset {
    id: string;
    name: string;
    metadata: string;
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
    TestType = "TEST_TYPE",
}

export enum AuthorityLevel {
    Read = "READ",
    Create = "CREATE",
    Update = "UPDATE",
    Delete = "DELETE",
}

export enum AuthorityType {
    GrantAuthority = "GRANT_AUTHORITY",
    Users = "USERS",
}

export enum RoleType {
    Admin = "ADMIN",
    User = "USER",
    Group = "GROUP",
    Test = "TEST",
    Service = "SERVICE",
    Special = "SPECIAL",
}

export enum EvaluationScenarioStatus {
    Started = "STARTED",
    Paused = "PAUSED",
    Resumed = "RESUMED",
    Stopped = "STOPPED",
}

export enum ClientEventType {
    Heartbeat = "HEARTBEAT",
    Notification = "NOTIFICATION",
    SimulationSciml = "SIMULATION_SCIML",
    SimulationPyciemss = "SIMULATION_PYCIEMSS",
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

export enum ProgrammingLanguage {
    Python = "python",
    R = "r",
    Julia = "julia",
    Zip = "zip",
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

export enum EquationType {
    Mathml = "mathml",
    Latex = "latex",
}

export enum ProvenanceType {
    Concept = "Concept",
    Dataset = "Dataset",
    Model = "Model",
    ModelConfiguration = "ModelConfiguration",
    Project = "Project",
    Publication = "Publication",
    Simulation = "Simulation",
    Artifact = "Artifact",
    Code = "Code",
    Document = "Document",
    Workflow = "Workflow",
}

export enum AssetType {
    Datasets = "datasets",
    ModelConfigurations = "model_configurations",
    Models = "models",
    Publications = "publications",
    Simulations = "simulations",
    Workflows = "workflows",
    Artifacts = "artifacts",
    Code = "code",
    Documents = "documents",
}

export enum OntologicalField {
    Object = "OBJECT",
    Unit = "UNIT",
}

export enum ExtractionAssetType {
    Figure = "FIGURE",
    Table = "TABLE",
    Equation = "EQUATION",
}
