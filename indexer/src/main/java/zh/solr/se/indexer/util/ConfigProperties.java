package zh.solr.se.indexer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties extends Properties {
	public static final String BOOST_FIELD_SEPARATOR = ",";
	public static final String NAME_VALUE_SEPARATOR  = ":";

	public static final String CONFIG_NAME_SOLR_HOME = "solr.solr.home";
	public static final String CONFIG_NAME_REMOTE_SOLR_HOST_CATEGORIZER = "remote.solr.host.categorizer";
	public static final String CONFIG_NAME_REMOTE_SOLR_HOST_LOCAL = "remote.solr.host.local";
	public static final String CONFIG_NAME_DEFAULT_ROWS = "search.default.rows";
	public static final String CONFIG_NAME_FIELD_BOOSTS_QA = "field.boost.qa";
	public static final String CONFIG_NAME_FIELD_BOOSTS_ARTICLE = "field.boost.article";

	// solr
	public static final String CONFIG_NAMR_SOLR_HOST = "solr.host";

	/**
	 * @Constructor
	 * @param filePath is the class path to the config file
	 */
	protected ConfigProperties(final String filePath) {
		final InputStream inStream = ConfigProperties.class.getResourceAsStream(filePath);
		try {
			load(inStream);
		} catch (final IOException e) {
			e.printStackTrace();
			throw new NullPointerException("Failed to load config file: " +
					filePath + ", error: " + e.getMessage());
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (final IOException e) {
					// do nothing
				}
			}

		}
	}

	public int getInt(final String propertyName, final int defaultValue) {
		int propertyValue = defaultValue;

		final String valueStr = getProperty(propertyName);
		try {
			propertyValue = Integer.parseInt(valueStr);
		} catch (final Exception e) {
			// do nothing, just return the default value;
		}

		return propertyValue;
	}

	public float getFloat(final String propertyName, final float defaultValue) {
		float propertyValue = defaultValue;

		final String valueStr = getProperty(propertyName);
		try {
			propertyValue = Float.parseFloat(valueStr);
		} catch (final Exception e) {
			// do nothing, just return the default value;
		}

		return propertyValue;
	}

	public double getDouble(final String propertyName, final double defaultValue) {
		double propertyValue = defaultValue;

		final String valueStr = getProperty(propertyName);
		try {
			propertyValue = Double.parseDouble(valueStr);
		} catch (final Exception e) {
			// do nothing, just return the default value;
		}

		return propertyValue;
	}
}
