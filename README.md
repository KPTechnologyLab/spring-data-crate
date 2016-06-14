# Spring Data Crate

The primary goal of the [Spring Data](http://projects.spring.io/spring-data)
project is to make it easier to build Spring-powered applications that use
new data access technologies such as non-relational databases, map-reduce
frameworks, and cloud based data services.

The Spring Data project aims to provide a familiar and consistent Spring-based
programming model for new datastores while retaining store-specific features
and capabilities. The Spring Data Crate project provides integration with the
[Crate](http://crate.io). [Crate](http://crate.io) is a shared-nothing,
fully searchable, document-oriented cluster data store. It is a quick and
powerful massively scalable backend for data intensive apps, like the
Internet of Things or real-time analytics.

## Features

* Automatic table export from entities using @Table annotation
* Support for mapping rich object model/graph to and from Crate
* Support for simple and composite Primary Key using @Id annotation.
  [Primary Key constraints](https://crate.io/docs/reference/sql/reference/create_table.html#primary-key-constraint)
* JSR 303 bean validation support
* Support for Lifecycle call back events (before/after save etc)
* Java based configuration

**NOTE:**
Composite primary key must contain [primitive field(s)](https://crate.io/docs/reference/sql/data_types.html#primitive-types)
supported by Crate.


## Quick Start

Checkout the project repository and issue the following command from
a shell/terminal.

```sh
mvn clean install
```

This will generate a jar file. The jar file can be found under the 'target'
folder and also in local maven repository in user's home directory in .m2folder.
Add the generated jar file in your project.

Please look at spring data crate's pom.xml for required dependencies.

### CrateTemplate

CrateTemplate is the central support class for Crate database operations.
It provides:

* Basic POJO mapping support to and from Crate Documents
* Convenience methods to interact with the store (insert, update, delete objects)
  and Crate specific ones (bulk insert, bulk update and bulk delete operations)
* Exception translation into Spring's [technology agnostic DAO exception hierarchy](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/dao.html#dao-exceptions).

### CrateRepository

To simplify the creation of data repositories Spring Data Crate provides a
generic repository programming model. A default implementation of
CrateRepository, aligning to the generic Repository Interfaces, is provided.

CrateRepository extends `CrudRepository` which causes CRUD methods being
pulled into the interface so that you can easily save and find single entities
and collections of them.

For example, given a `User` class with Id property of type String, a
`UserRepository` interface can be defined as shown below:

```java
public interface UserRepository extends CrateRepository<User, String> {
}
```

**NOTE:**
Currently documents can be queried by Id. Support will be added to derive
queries from method names.

#### Query Methods

##### Query Lookup Strategies

The Spring Data Crate can resolve a query using following
query lookup strategies:

* `CREATE` constructs a query from the query method name of a
  repository.
* `USE_DECLARED_QUERY` looks up for a declared query and will throw an
  exception in case it is not found.
* `CREATE_IF_NOT_FOUND` checks first for declared query, if it is not
  found, it uses the `CREATE` strategy to create a query from a method
  name.

The strategy can be configured at namespace via XML by setting the
query lookup strategy attribute or through the `queryLookupStrategy`
attribute of the `@EnableCrateRepositories` annotation when using Java
configurations.

###### Query creation

When using the `CREATE` query lookup strategy the Spring Data Crate
repository infrastructure constructs a query from a method name. It
strips `find..By`, `read..By`, `query..By`, and `get..` prefixes from
the method name. Then, the query is build by parsing the rest of the
method name and the entity metadata.

Expressions in a method name can be combined with `AND` and `OR`.

The query creation mechanism also supports following operators:
`[is]greaterThan`, `[is]greaterThanEqual`, `[is]lessThan`,
`[is]lessThanEqual`, `[is]Equals`, `Like`, `[is]Start[s|ing]With`,
`End[s|ing]With`, `[is]Null`, `[is]True`, and `[is]False`.

```java
public interface UserRepository extends CrateRepository<User, String> {

    List<User> findByNameAndAge(String name, Integer age);
    
    List<User> findByNameOrAge(String name, Integer age);
    
    User getByEmail(String email);
    
    List<User> findByAgeGreaterThanEqual(Integer age);
}
```

###### Declared Queries

It's also possible to use the `@Query` annotation to define queries:

```java
public interface UserRepository extends CrateRepository<User, String> {

    @Query("select * from users")
    List<User> getAllUsers();
}
```

The query annotation can also be used with
[parameter substitution](https://crate.io/docs/reference/sql/rest.html#parameter-substitution),
where the arguments passed to the method has to match the positional
placeholders in the query:


```java
public interface UserRepository extends CrateRepository<User, String> {

    @Query("select * from users where name=$1 and age=$2")
    List<User> findUsersByNameAndAge(String name, int age);
}
```

#### Application config

You can have Spring automatically create a proxy for the interface by using
the following JavaConfig:

```java
@Configuration
@EnableCrateRepositories
class ApplicationConfig extends AbstractCrateConfiguration {
}
```

This sets up a connection to a Crate instance and enables the detection of
Spring Data repositories (through `@EnableCrateRepositories`).

Custom converters can be added by overridding methods provided in
AbstractCrateConfiguration class.

By default, a crate client will be created to connect to Crate running on
`localhost` on port `4300`. A custom host and port can be specified as
shown below:

```java
@Configuration
@EnableCrateRepositories
class ApplicationConfig extends AbstractCrateConfiguration {

  @Override
  public CrateClient crateClient() {
		return new CrateClient("<host>:<port>");
  }
}
```
The same configuration would look like this in XML:

```xml
<bean name="crateTemplate" class="org.springframework.data.crate.core.CrateTemplate">
	<constructor-arg ref="client"/>
</bean>

<crate:client id="client" servers="127.0.0.1:4300"/>

<crate:repositories base-package="com.acme.repository"/>
```

The `servers` attribute takes a comma separated string of `host:port`

**NOTE:**
Currently adding custom converters via XML is not supported.

This will find the repository interface and register a proxy object in the
container. You can use it as shown below:

```java
@Table(name="users", numberOfReplicas="2", refreshInterval=1500, columnPolicy=ColumnPolicy.DYNAMIC)
public class User {

	@Id
	private String id;

	// JSR-303 annotations
	@Email
	@NotBlank
	private String email;

	/** Optional. If other constructors are defined, then annotate one of them
	@PersistenceConstructor
	public User(String id, String email) {
		this.id = id;
		this.email = email;
	}**/

	// getters and setters
}
```
Changes to the `numberOfReplicas` table parameter will be reflected to the
database on application restarts. Details about table parameters can be found
here [Table Parameters](https://crate.io/docs/stable/sql/reference/create_table.html)

**NOTE:**
Crate currently does not return `refresh_interval` and `column_policy` from
`information_schema.tables`. To change the values of these two table parameters,
you will have to execute the queries manually from Crate's `crash` command
line tool.

```sh
alter table my_table set (refresh_interval = <NEW_VALUE>);
alter table my_table set (column_policy = <NEW_VALUE>);
```

```java
@Service
public class UserService {

  private UserRepository repository;

  @Autowired
  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public void doStuff() {

     repository.deleteAll();

     User user = new User();
     user.setId("abc123");
     user.setEmail("user@acme.com");

     repository.save(user);

	 repository.refreshTable();

     User dbUser = repository.findById(user.getId());
     List<User> users = repository.findAll();
 }

}
```

**NOTE:**
Information about table refresh can be found here
[Refresh](https://crate.io/docs/stable/sql/refresh.html)

### Schema Export

Spring Data Crate can export tables from entities annotated with `@Table`
annotation. By default, the table name will be derived from the entity package
and class name. Otherwise, the name will be used from the name attribute of
the annotation `@Table(name="myTable")`.

To enable automatic creation of tables, register the following bean as shown
below:

```java
@Configuration
@EnableCrateRepositories
class ApplicationConfig extends AbstractCrateConfiguration {

	@Bean
	public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
		return new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP);
	}
}
```

The CratePersistentEntitySchemaManager can be configured to ignore failures
and enabled (useful if using the spring's environment abstraction to
enable/disable schema export) as follows:

```java
@Configuration
@EnableCrateRepositories
class ApplicationConfig extends AbstractCrateConfiguration {

	@Bean
	public CratePersistentEntitySchemaManager cratePersistentEntitySchemaManager() throws Exception {
		CratePersistentEntitySchemaManager manager = new CratePersistentEntitySchemaManager(crateTemplate(), CREATE_DROP)
		manager.setIgnoreFailures(true); // defaults to false
		manager.setEnabled(false); // defaults to true
		return manager;
	}
}
```

If the CratePersistentEntitySchemaManager is not enabled, tables will not be
exported to Crate.

Full configuration would look like this in XML:

```xml
<bean name="crateTemplate" class="org.springframework.data.crate.core.CrateTemplate">
	<constructor-arg ref="client"/>
</bean>

<crate:client id="client" servers="127.0.0.1:4300"/>

<crate:repositories base-package="com.acme.repository"/>

<crate:schema-export ignoreFailures="false" export-option="CREATE_DROP" enabled="true"/>
```

There are 3 schema export options

```sh
- CREATE
- CREATE_DROP
- UPDATE
```

`CREATE` will create the tables if they do not exist.

`CRATE_DROP` will do the same as `CREATE` but will drop all the tables on
application shutdown.

`UPDATE` will create the tables if they do not exist. Otherwise, the tables
will be updated if new fields are added to the entities resulting in new
columns being added to the tables.
