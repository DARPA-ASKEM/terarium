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

## Hibernate Relationships:

There are three types of relationship annotations used in **Hibernate**:
- `@OneToOne <-> @OneToOne`: a single parent entity references 0 or 1 child entity.
- `@OneToMany <-> @ManyToOne`: a single parent entity references 0 to n child entities.
- `@ManyToMany <-> @ManyToMany`: multiple parent entities share 0 to n child entities.

Mapping to the tables:

`@OneToOne` and `@OneToMany` can be done with or without an additional table, while `@ManyToMany` must use an additional table.

### Defining Bi-directional Relationships

For `@OneToOne` and `@OneToMany` bi-directional relationships we prefer to not use an additional table.

These relationships are setup as follows:

- `@OneToOne` / `@OneToMany` / `@ManyToOne` / `@ManyToMany` annotations are used to define the type of relationship between the entities.
    - `mappedBy` defines which side "owns" the relationship. For `OneToOne` and `@OneToMany` it defines which side _does not_ contain the foreign key. If `mappedBy` is not specified, an additional table is used to store the relationship. For `@ManyToMany` relationships, it is used to define the "owner" side of the many to many.
    - `cascade` defines what types of operations are cascaded from the owner to the other side of the relationship. `CascadeType.ALL` seems to be a safe default.
    - `fetch` defines how the elemeents are fetched. `EAGER` fetches them immediately. `LAZY` fetches them only when accessed.

- `@JsonManagedReference` and `@JsonBackReference` are required to prevent `toString` and `hashCode` from exploding due to the circular references of bi-directional relationships.

**Note:** because we use `mappedBy` to define the ownsership of the relationship, `@JoinColumn` is not necessary for bi-directional relationships.

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

### Defining Uni-directional Relationships

Uni-directional relationships are where only one side of the reference is defined in the code, inside the "owning" entity. For these relationships the `mappedBy` argument is omitted and the relationship is stored depends on the `@JoinColumn` annotation. By default if `@JoinColumn` is not defined, an additional table is created to define the relationship.

How the `@JoinColumn` annotation is used is based on the type of relationship, as per the docs:

```html
/*
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
