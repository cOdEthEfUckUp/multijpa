package tw.codethefuckup.multijpa;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 主數據源配置
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
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
