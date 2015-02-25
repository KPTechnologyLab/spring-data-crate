# Spring Data Crate

The primary goal of the [Spring Data](http://projects.spring.io/spring-data) project is to make it easier to build Spring-powered applications that use new data access technologies such as non-relational databases, map-reduce frameworks, and cloud based data services.

The Spring Data project aims to provide a familiar and consistent Spring-based programming model for new datastores while retaining store-specific features and capabilities. The Spring Data Crate project provides integration with the [Crate Data](http://crate.io). [Crate Data](http://crate.io) is a shared-nothing, fully searchable, document-oriented cluster data store. It is a quick and powerful massively scalable backend for data intensive apps, like the Internet of Things or real-time analytics.

## Features

* Automatic table export from entities using @Table annotation
* Support for mapping rich object model/graph to and from Crate
* Support for simple and complex Primary Key using @Id annotation
* JSR 303 bean validation support
* Support for Lifecycle call back events (before/after save etc)
* Java based configuration

## Quick Start

Checkout the project repository and issue the following command from a shell/terminal

```sh
mvn clean install
```
Add the generated jar file in your project

### CrateTemplate

CrateTemplate is the central support class for Crate database operations. It provides:

* Basic POJO mapping support to and from Crate Documents
* Convenience methods to interact with the store (insert, update, delete objects) and Crate specific ones (bulk insert, bulk update and bulk delete operations)
* Exception translation into Spring's [technology agnostic DAO exception hierarchy](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/dao.html#dao-exceptions).

### CrateRepository

To simplify the creation of data repositories Spring Data Crate provides a generic repository programming model. A default implementation of CrateRepository, aligning to the generic Repository Interfaces, is provided.  

CrateRepository extends `CrudRepository` which causes CRUD methods being pulled into the interface so that you can easily save and find single entities and collections of them.

For example, given a `User` class with Id property of type String, a `UserRepository` interface can be defined as shown below:

```java
public interface UserRepository extends CrateRepository<User, String> {
}
```

**NOTE:**
Currently documents can be queried by Id. Support will be added to derive queries from method names.

You can have Spring automatically create a proxy for the interface by using the following JavaConfig:

```java
@Configuration
@EnableCrateRepositories
class ApplicationConfig extends AbstractCrateConfiguration {
}
```
This sets up a connection to a Crate instance and enables the detection of Spring Data repositories (through `@EnableCrateRepositories`)

Custom converters can be added by overridding methods provided in AbstractCrateConfiguration class.

By default, a crate client will be created to connect to Crate running on `localhost` on port `4300`. A custom host and port can be specified as shown below:

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

This will find the repository interface and register a proxy object in the container. You can use it as shown below:

```java
@Table(name="users")
public class User {
	
	@Id
	private String id;
	
	// JSR-303 annotations
	@Email
	@NotBlank
	private String email;
	
	/** optional
	@PersistenceConstructor
	public User(String id, String email) {
		this.id = id;
		this.email = email;
	}**/
	
	// getters and setters
}
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
     user.setId(1L);
     user.setEmail("user@acme.com");
     
     repository.insert(user);

     User dbUser = repository.findById(user.getId());
     List<User> users = repository.findAll();
 }
}
```

### Schema Export

Spring Data Crate can export tables from entities annotated with `@Table` annotation. By default, the table name will be derived from the entity package and class name. Otherwise, the name will be used from the name attribute of the annotation `@Table(name="myTable")`.

To enable automatic creation of tables, register the following bean as shown below:

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
The CratePersistentEntitySchemaManager can be configured to ignore failures and enabled (useful if using the spring's environment abstraction to enable/disable schema export) as follows:

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

If the CratePersistentEntitySchemaManager is not enabled, tables will not be exported to Crate.

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

`CRATE_DROP` will do the same as `CREATE` but will drop all the tables on application shutdown.

`UPDATE` will create the tables if they do not exist. Otherwise, the tables will be updated if new fields are added to the entities resulting in new columns being added to the tables.   