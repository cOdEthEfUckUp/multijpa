package tw.codethefuckup.multijpa;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 次要數據源配置
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
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
