# Typescript Model Generation

## Motivation

We use a [gradle plugin](https://github.com/vojtechhabarta/typescript-generator) to automatically generate typescript types that need to be shared between the client and server. 
It's common that we need to defined "mirrored" types in the client and server. These can go out of date and cause bugs. 
This PR allows us to specify such types only on the server to ensure they're stay in sync.

## Example

Types are generated on an opt-in basis and controlled via annotations.  For example:
```java
public enum DataRequestType {
  SIGNED,
  SEALED,
  DELIVERED
}

@TSModel
public class DataRequest {
  private String id;
  private String targetId;
  @TSOptional
  private String description;
  private DataRequestType type;
  
  @TSIgnore
  public String internalId() {
      return id + "_" + targetId;
  }
}
```
will generate the following in `Types.ts`:
```typescript
export interface DataRequest {
    id: string;
    targetId: string;
    description?: string;
    type: DataRequestType;
}

export enum DataRequestType {
    Signed = "SIGNED",
    Sealed = "SEALED",
    Delivered = "DELIVERED",
}
```

## Annotations and Notes
`@TSModel`: The model class should have a corresponding generated interface
`@TSIgnore`: The field should be excluded from the generated model interface
`@TSOptional`: The field should be optional in the generated interface

An important point is that any class or type that is included in a `@TSModel` annotated class will be pulled in as well
regardless of whether or not that class is annotated with `@TSModel`

## Usage
Model files are watched by default when running `yarn dev`.  If a model file is changed that requires a TS type
it will be automatically rebuilt and thus will subsequently trigger a Vite rebuild. 
tl;dr `yarn dev` and the rest is handled.

If you are running _only_ the HMI server, you must manually run:
```bash
./gradlew generateTypeScript
```
from the root directory before you commit in the instance that you have modified
`@TSModel` files
