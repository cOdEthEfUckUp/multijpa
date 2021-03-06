# 具有多個 `DataSource` 的 Spring Data JPA

[TOC]

###### tags: `spring`

---

## 目標

在一個 [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/) 專案中同時使用兩個以上的 `DataSource` 並為每個 `DataSource` 配置 [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/)。

## 情境

- 主要的`DataSource`為 [PostgreSQL](https://www.postgresql.org/)，以 Apache Tomcat JDBC 為 connection pool。
- 次要的`DataSource`為 [MariaDB](https://mariadb.org/)，以 Hikari 為 connection pool。

## 專案結構

```
├─ pom.xml
└─ src/
   └─ main/
      ├─ java/
      │  └─ tw/
      │     └─ codethefuckup/
      │        └─ multijpa/
      │           ├─ Application.java
      │           ├─ DatasourceConfiguration.java
      │           ├─ PrimaryDatasourceConfiguration.java
      │           ├─ SecondaryDatasourceConfiguration.java
      │           ├─ WebMvcConfigurerImpl.java
      │           ├─ controller/
      │           │  └─ WelcomeController.java
      │           ├─ entity/
      │           │  ├─ primary/
      │           │  |  └─ MercedesBenz.java
      │           │  └─ secondary/
      │           │     └─ BayerischeMotorenWerke.java
      │           └─ repository/
      │           ├─ primary/
      │           │  └─ MercedesRepository.java
      │           └─ secondary/
      │              └─ BmwRepository.java
      └─ resources/
         └─ application.properties
```

## 配置

### `src/main/resources/application.properties`

```
logging.level.root=${LOG_LEVEL:info}

server.compression.enabled=true
server.compression.min-response-size=1KB
server.error.whitelabel.enabled=false
server.servlet.register-default-servlet=true
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=combined

spring.devtools.restart.enabled=false
spring.application.name=multijpa
spring.jackson.serialization.indent-output=false
spring.output.ansi.enabled=always

# Hibernate/JPA 相關配置
spring.jpa.hibernate.ddl-auto=none
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.open-in-view=false
spring.jpa.show-sql=true

# 主要 DataSource 的配置
datasource.primary.driverClassName=${PRIMARY_DRIVER_CLASS_NAME}
datasource.primary.password=${PRIMARY_PASSWORD}
datasource.primary.port=${PRIMARY_PORT}
datasource.primary.type=org.apache.tomcat.jdbc.pool.DataSource
datasource.primary.url=jdbc:postgresql://${PRIMARY_HOST}:${PRIMARY_PORT}/${PRIMARY_CATALOG}?user=${PRIMARY_USERNAME}&password=${PRIMARY_PASSWORD}
datasource.primary.username=${PRIMARY_USERNAME}

# 次要 DataSource 的配置
datasource.secondary.driverClassName=${SECONDARY_DRIVER_CLASS_NAME}
datasource.secondary.password=${SECONDARY_PASSWORD}
datasource.secondary.port=${SECONDARY_PORT}
datasource.secondary.type=org.mariadb.jdbc.MariaDbPoolDataSource
datasource.secondary.url=jdbc:mariadb://${SECONDARY_HOST}:${SECONDARY_PORT}/${SECONDARY_CATALOG}?user=${SECONDARY_USERNAME}&password=${SECONDARY_PASSWORD}
datasource.secondary.username=${SECONDARY_USERNAME}
```

### 所有 `DataSource` 共用的配置

若有共用的設定(如 connection pools)，若無則可省略。

```java
public class DatasourceConfiguration {

	public static final int INITIAL_SIZE = Integer.parseInt(
		System.getenv("DATASOURCE_INITIAL_SIZE")
	);

	public static final int MAX_ACTIVE = Integer.parseInt(
		System.getenv("DATASOURCE_MAX_ACTIVE")
	);

	public static final int MAX_IDLE = Integer.parseInt(
		System.getenv("DATASOURCE_MAX_IDLE")
	);

	public static final int MAX_WAIT = Integer.parseInt(
		System.getenv("DATASOURCE_MAX_WAIT")
	);

	public static final int MIN_IDLE = Integer.parseInt(
		System.getenv("DATASOURCE_MIN_IDLE")
	);

	public static final boolean REMOVE_ABANDONED = Boolean.parseBoolean(
		System.getenv("DATASOURCE_MIN_IDLE")
	);

	public static final boolean ROLLBACK_ON_RETURN = Boolean.parseBoolean(
		System.getenv("DATASOURCE_MIN_IDLE")
	);

	public static final String VALIDATION_QUERY = System.getenv(
		"DATASOURCE_VALIDATION_QUERY"
	);
}
```

### 主要 `DataSource` 的配置

```java=24
@Configuration
@EnableJpaRepositories(
	basePackages = {"tw.codethefuckup.multijpa.repository.primary"},
	entityManagerFactoryRef = "primaryEntityManagerFactory",
	transactionManagerRef = "primaryTransactionManager"
)
@EnableTransactionManagement
public class PrimaryDatasourceConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryDatasourceConfiguration.class);

	/**
	 * 從系統環境變數取得主 database 名稱
	 */
	private static final String DATASOURCE_CATALOG = System.getenv("PRIMARY_CATALOG");

	/**
	 * 從 application.properties 讀取主 DataSource 的設定
	 *
	 * @return 主 DataSource 的設定
	 */
	@Bean
	@ConfigurationProperties("datasource.primary")
	@Primary
	public DataSourceProperties primaryDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * 初始化主 DataSource
	 *
	 * @return 主 DataSource
	 */
	@Bean(name = "primaryDataSource")
	@ConfigurationProperties("datasource.primary.configuration")
	@Primary
	public DataSource primaryDataSource() {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = primaryDataSourceProperties().
			initializeDataSourceBuilder().
			type(org.apache.tomcat.jdbc.pool.DataSource.class).
			build();

		dataSource.setDefaultAutoCommit(false);
		dataSource.setDefaultCatalog(DATASOURCE_CATALOG);
		dataSource.setInitialSize(DatasourceConfiguration.INITIAL_SIZE);
		dataSource.setMaxActive(DatasourceConfiguration.MAX_ACTIVE);
		dataSource.setMaxIdle(DatasourceConfiguration.MAX_IDLE);
		dataSource.setMaxWait(DatasourceConfiguration.MAX_WAIT);
		dataSource.setMinIdle(DatasourceConfiguration.MIN_IDLE);
		dataSource.setRemoveAbandoned(DatasourceConfiguration.REMOVE_ABANDONED);
		dataSource.setRollbackOnReturn(DatasourceConfiguration.ROLLBACK_ON_RETURN);
		dataSource.setValidationQuery(DatasourceConfiguration.VALIDATION_QUERY);

		return dataSource;
	}

	/**
	 * 初始化主 DataSource 的 EntityManager
	 *
	 * @param entityManagerFactoryBuilder 供主 EntityManager 用的工廠建造器
	 * @return 主 EntityManager 工廠
	 */
	@Bean(name = "primaryEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
		EntityManagerFactoryBuilder entityManagerFactoryBuilder
	) {
		return entityManagerFactoryBuilder.
			dataSource(primaryDataSource()).
			packages("tw.codethefuckup.multijpa.entity.primary").
			build();
	}

	/**
	 * 初始化主 EntityManager 的交易管理
	 *
	 * @param localContainerEntityManagerFactoryBean 主 EntityManager 工廠
	 * @return 主 EntityManager 的交易管理
	 */
	@Bean
	@Primary
	public PlatformTransactionManager primaryTransactionManager(
		@Qualifier("primaryEntityManagerFactory") final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
	) {
		return new JpaTransactionManager(
			localContainerEntityManagerFactoryBean.getObject()
		);
	}
}
```

### 次要 `DataSource` 的配置

```java=24
@Configuration
@EnableJpaRepositories(
	basePackages = {"tw.codethefuckup.multijpa.repository.secondary"},
	entityManagerFactoryRef = "secondaryEntityManagerFactory",
	transactionManagerRef = "secondaryTransactionManager"
)
@EnableTransactionManagement
public class SecondaryDatasourceConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryDatasourceConfiguration.class);

	/**
	 * 從系統環境變數取得次要 database 名稱
	 */
	private static final String DATASOURCE_CATALOG = System.getenv("SECONDARY_CATALOG");

	/**
	 * 從 application.properties 讀取次要 DataSource 的設定
	 *
	 * @return 次 DataSource 的設定
	 */
	@Bean
	@ConfigurationProperties("datasource.secondary")
	public DataSourceProperties secondaryDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * 初始化次要 DataSource
	 *
	 * @return 次要 DataSource
	 */
	@Bean(name = "secondaryDataSource")
	@ConfigurationProperties("datasource.secondary.configuration")
	public DataSource secondaryDataSource() {
		HikariDataSource dataSource = secondaryDataSourceProperties().
			initializeDataSourceBuilder().
			type(HikariDataSource.class).
			build();

		dataSource.setAutoCommit(false);
		dataSource.setCatalog(DATASOURCE_CATALOG);
		dataSource.setIdleTimeout(DatasourceConfiguration.MAX_IDLE);
		dataSource.setMaximumPoolSize(DatasourceConfiguration.MAX_ACTIVE);
		dataSource.setMaxLifetime(DatasourceConfiguration.MAX_WAIT);
		dataSource.setMinimumIdle(DatasourceConfiguration.MIN_IDLE);

		return secondaryDataSourceProperties().
			initializeDataSourceBuilder().
			type(HikariDataSource.class).
			build();
	}

	/**
	 * 初始化次要 DataSource 的 EntityManager
	 *
	 * @param entityManagerFactoryBuilder 供次要 EntityManager 用的工廠建造器
	 * @return 次要 EntityManager 工廠
	 */
	@Bean(name = "secondaryEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
		EntityManagerFactoryBuilder entityManagerFactoryBuilder
	) {
		return entityManagerFactoryBuilder.
			dataSource(secondaryDataSource()).
			packages("tw.codethefuckup.multijpa.entity.secondary").
			build();
	}

	/**
	 * 初始化次要 EntityManager 的交易管理
	 *
	 * @param localContainerEntityManagerFactoryBean 次要 EntityManager 工廠
	 * @return 次要 EntityManager 的交易管理
	 */
	@Bean
	public PlatformTransactionManager secondaryTransactionManager(
		@Qualifier("secondaryEntityManagerFactory") final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
	) {
		return new JpaTransactionManager(
			localContainerEntityManagerFactoryBean.getObject()
		);
	}
}
```

### Web MVC 的配置

```java=17
@Configuration
public class WebMvcConfigurerImpl implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebMvcConfigurerImpl.class);

	@Bean(name = "primaryJdbcTemplate")
	public JdbcTemplate primaryJdbcTemplate(
		@Qualifier("primaryDataSource") DataSource dataSource
	) {
		return new JdbcTemplate(dataSource);
	}

	@Bean(name = "secondaryJdbcTemplate")
	public JdbcTemplate secondaryJdbcTemplate(
		@Qualifier("secondaryDataSource") DataSource dataSource
	) {
		return new JdbcTemplate(dataSource);
	}
}
```

## 概念驗證

### `src/main/java/tw/codethefuckup/multijpa/controller/WelcomeController.java`

```java=21
@RestController
@RequestMapping("/")
public class WelcomeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(WelcomeController.class);

	@Autowired
	private BmwRepository bmwRepository;

	@Autowired
	private MercedesRepository mercedesRepository;

	@GetMapping("benz")
	@ResponseBody
	List<MercedesBenz> somethingFromPrimaryDataSource() {
		return mercedesRepository.findAll();
	}

	@GetMapping("bmw")
	@ResponseBody
	List<BayerischeMotorenWerke> somethingFromSecondaryDataSource() {
		return bmwRepository.findAll();
	}
}
```
