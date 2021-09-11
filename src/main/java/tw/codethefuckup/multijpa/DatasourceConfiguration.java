package tw.codethefuckup.multijpa;

/**
 * @author P-C Lin (a.k.a 高科技黑手)
 */
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
