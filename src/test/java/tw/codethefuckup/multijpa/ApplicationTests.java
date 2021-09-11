package tw.codethefuckup.multijpa;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.AssertionErrors;

@SpringBootTest
class ApplicationTests {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ApplicationTests.class);

	private static final String SQL = "SELECT NULL";

	@Autowired
	private org.apache.tomcat.jdbc.pool.DataSource primaryDataSource;

	@Autowired
	private com.zaxxer.hikari.HikariDataSource secondaryDataSource;

	@Test
	void contextLoads() {
		AssertionErrors.assertNotNull("主數據源", primaryDataSource);

		try {
			Connection connection = primaryDataSource.getConnection();
			AssertionErrors.assertNotNull("主連線", connection);
			AssertionErrors.assertEquals(
				"主數據庫",
				System.getenv("PRIMARY_CATALOG"),
				connection.getCatalog()
			);
			AssertionErrors.assertTrue(
				"主連線執行陳述",
				connection.createStatement().execute(SQL)
			);
		} catch (SQLException sqlException) {
			LOGGER.info(null, sqlException);
		}

		try {
			Connection connection = secondaryDataSource.getConnection();
			AssertionErrors.assertNotNull("次要連線", connection);
			AssertionErrors.assertEquals(
				"次要數據庫",
				System.getenv("SECONDARY_CATALOG"),
				connection.getCatalog()
			);
			AssertionErrors.assertTrue(
				"次要連線執行陳述",
				connection.createStatement().execute(SQL)
			);
		} catch (SQLException sqlException) {
			LOGGER.info(null, sqlException);
		}
	}
}
