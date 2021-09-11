package tw.codethefuckup.multijpa;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置 Web MVC
 *
 * @author P-C Lin (a.k.a 高科技黑手)
 */
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
