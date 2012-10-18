package zh.solr.se.indexer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import zh.solr.se.indexer.util.ConfigFactory;


public class DbService {
	// The following are logic database names. They will be mapped to real names using config file
	public static final String DB_LOCAL  = "localdb";
	public static final String DB_GNOSIS = "gnosis";
	public static final String DB_AUTO = "cars";
	public static final String DB_BENCHMARK = "benchmark";
	public static final String DB_AROUNDME = "aroundme";
	public static final String DB_SUGGEST = "suggest";

	private static DbService instance;

	private final Properties dbProperties;
	String url;
	String username;
	String password;

	private DbService() throws Exception {
		// load property file
		dbProperties = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.DB_CONFIG_PATH);
		url = dbProperties.getProperty("db.url");
		username = dbProperties.getProperty("db.username");
		password = dbProperties.getProperty("db.password");

		// initialize the DB driver
		final String driver = dbProperties.getProperty("jdbc.driver");
		Class.forName(driver);
	}

	synchronized public static DbService getInstance() throws Exception {
		if (instance == null) {
			instance = new DbService();
		}

		return instance;
	}

	public Connection getConnection(final String dbName) {
		return getConnection(dbName, url);
	}

	public Connection getConnection(final String dbName, String url) {
		// use default URL if the url is null
		if (url == null) {
			url = this.url;
		}

		if (dbName == null) {
			throw new NullPointerException("Database name must not be null");
		}

		Connection conn = null;
		try {
			//conn = DriverManager.getConnection(url + "/" + dbName + "?useServerPrepStmts=false"
			conn = DriverManager.getConnection(url + "/" + dbName
								+ "?&user=" + username
								+ "&password=" + password);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to make a DB connection to " + url + ", error: " + e.getMessage());
		}

		return conn;
	}
}
