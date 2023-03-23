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

export interface Ontology {
    name: string;
    curie: string;
    title?: string;
    description?: string;
    link?: string;
}

export interface Species {
    sname: string;
    miraIds?: Ontology[];
    miraContext?: Ontology[];
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
