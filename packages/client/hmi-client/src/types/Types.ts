/* tslint:disable */
/* eslint-disable */

export interface ClientConfig {
    baseUrl: string;
    clientLogShippingEnabled: boolean;
    clientLogShippingIntervalMillis: number;
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

export interface Role {
    id: number;
    name: string;
    authorities: AuthorityInstance[];
}

export interface AuthorityInstance {
    id: number;
    mask: number;
    authority: Authority;
}

export interface Authority {
    id: number;
    name: string;
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
    Special = "SPECIAL",
}
