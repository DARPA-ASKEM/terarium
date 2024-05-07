# Terarium Server

This is the main Terarium Server application.

## Docker Images

There are two versions of the docker images. One is a regular JVM build and the other is an optimized native build. Each can be built individually with the following commands:

> NOTE: BuildX should be enabled (if using latest Docker Desktop with enging >=v20 this should be automatically enabled for you)

> Docker files are set to be run with the root context

### Building JVM Image

To build the JVM image simply run the following from the root. Alternatively change the location of the Dockerfile if running from within the server directory
```sh
docker buildx build -f modules/server/docker/Dockerfile -t docker.uncharted.software/terarium:server .
```

### Building Native Image
To build the NATIVE image simply run the following from the root directory.
```sh
docker buildx build -f modules/server/docker/Dockerfile.native -t docker.uncharted.software/terarium:server .
```

### Hibernate Reference:

There are three types of relationships in `hibernate`: `@OneToOne`, `@OneToMany`/`@ManyToOne`, and `@ManyToMany`.

`@OneToOne` and `@OneToMany` can be done without an additional table, while `@ManyToMany` must use an additional table.

#### Defining Bi-directional Relationships

For `@OneToOne` and `@OneToMany` bi-directional relationships we prefer to not use an additional table. These relationships are setup as follows:

- `@OneToMany` and `@ManyToOne` annotations are used to define the type of relationship between the entities.
    - `mappedBy` defines which part of the relationship _does not_ contain the foreign key, typically the "parent". If `mappedBy` is not specified, an additional table is used to store the relationship.
    - `cascade` defines what types of operations are cascaded to the other side of the relationship.
    - `fetch` defines how the elemeents are fetched. `EAGER` fetches them immediately. `LAZY` fetches them only when accessed.

- `@JsonManagedReference` and `@JsonBackReference` are required to prevent `toString` and `hashCode` from exploding due to the circular references of bi-directional relationships.

For `@ManyToMany`, simply omit `mappedBy` from the annotation.

```java
@Data
@Entity
class Parent {

    @Id
    private UUID id = UUID.randomUUID();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Child> children = new ArrayList<>();

};

@Data
@Entity
class Child {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @JsonBackReference
    @NotNull
    private Parent parent;

};
```

#### Defining Uni-directional Relationships

Uni-directional relationships are where only one side of the reference is defined. This is typically used when some child type may be "owned" by various parent types.


 In these cases we use the `@JoinColumn` on the parent to specify the foreign key field on the child. If the `@JoinColumn` is not used, by default `hibernate` will create an additional table to store the relationship.

```java
@Data
@Entity
class Parent {

    @Id
    private UUID id = UUID.randomUUID();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id") // <--- because this is a @OneToMany uni-directional relationship, its referencing the column on the child, not the parent.
    private List<Child> children = new ArrayList<>();

};

@Data
@Entity
class Child {

    @Id
    private UUID id = UUID.randomUUID();

};
```

**NOTE:** `@JoinColumn` is a tricky annotation because it is interpreted differently based on what relationship annotation it is paired with, and whether the relationship is uni-directional vs bi-directional.

```html
/*
* (Optional) The name of the foreign key column.
* The table in which it is found depends upon the
* context.

* - If the join is for a OneToOne or ManyToOne
*  mapping using a foreign key mapping strategy,
* the foreign key column is in the table of the
* source entity or embeddable.

* - If the join is for a unidirectional OneToMany mapping
* using a foreign key mapping strategy, the foreign key is in the
* table of the target entity.

* - If the join is for a ManyToMany mapping or for a OneToOne
* or bidirectional ManyToOne/OneToMany mapping using a join
* table, the foreign key is in a join table.

* -  If the join is for an element collection, the foreign
* key is in a collection table.
*/
```
