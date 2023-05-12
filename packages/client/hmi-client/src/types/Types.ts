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

export interface Annotations {
    geo: DatasetAnnotatedGeo[];
    date: DatasetAnnotatedDate[];
    feature: DatasetAnnotatedFeature[];
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
    temporalResolution: string;
    geospatialResolution: string;
    annotations: DatasetAnnotations;
    maintainer: string;
    simulationRun: boolean;
}

export interface DatasetAnnotatedDate extends DatasetAnnotatedField {
    dateType: string;
    primaryDate: boolean;
    timeFormat: string;
}

export interface DatasetAnnotatedFeature extends DatasetAnnotatedField {
    featureType: string;
    units: string;
    unitsDescription: string;
    primaryOntologyId: string;
    qualifierRole: string;
}

export interface DatasetAnnotatedField {
    name: string;
    displayName: string;
    description: string;
    type: string;
    qualifies: string[];
    aliases: any;
}

export interface DatasetAnnotatedGeo extends DatasetAnnotatedField {
    geoType: string;
    primaryGeo: boolean;
    resolveToGadm: boolean;
    isGeoPair: string;
    coordFormat: string;
    gadmLevel: string;
}

export interface DatasetAnnotations {
    dataPaths: string[];
    annotations: Annotations;
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

export interface SimulationParams {
    petri: string;
    initials: { [index: string]: number };
    tspan: number[];
    params: { [index: string]: number };
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
