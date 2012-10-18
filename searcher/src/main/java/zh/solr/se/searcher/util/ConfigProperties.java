package zh.solr.se.searcher.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties extends Properties {
	public static final String FIELD_SEPARATOR = ",";
	public static final String NAME_VALUE_SEPARATOR  = ":";
	
	public static final String CONFIG_NAME_DEFAULT_ROWS = "search.default.rows";
	public static final String CONFIG_NAME_FIELD_BOOSTS_QA = "field.boost.qa";
	public static final String CONFIG_NAME_ONTOLOGY_BOOSTS_QA = "ontology.boost.qa";
	public static final String CONFIG_NAME_MAX_CATEGOERY_LEVEL_EXCLUDE_QA = "max_category_level_exclude.qa";
	public static final String CONFIG_NAME_CUTOFF_SCORE_QA = "cutoff_score.qa";
	public static final String CONFIG_NAME_SCALE_FACTORS_QA = "scale_factors.qa";
	public static final String CONFIG_NAME_FIELD_BOOSTS_ARTICLE = "field.boost.article";
	public static final String CONFIG_NAME_ONTOLOGY_BOOSTS_ARTICLE = "ontology.boost.article";
	public static final String CONFIG_NAME_MAX_CATEGOERY_LEVEL_EXCLUDE_ARTICLE = "max_category_level_exclude.article";
	public static final String CONFIG_NAME_CUTOFF_SCORE_ARTICLE = "cutoff_score.article";
	public static final String CONFIG_NAME_SCALE_FACTORS_ARTICLE = "scale_factors.article";
	public static final String CONFIG_NAME_FIELD_BOOSTS_RECIPE = "field.boost.recipe";
	public static final String CONFIG_NAME_CUTOFF_SCORE_RECIPE = "cutoff_score.recipe";
	public static final String CONFIG_NAME_SCALE_FACTORS_RECIPE = "scale_factors.recipe";
	public static final String CONFIG_NAME_SOURCE_WEIGHTS_GEOIP = "source_weights.geoip";
	public static final String CONFIG_NAME_TOTAL_WEIGHT_GEOIP = "max.total_weight.geoip";
	
	/*sink config name*/
	public static final String CONFIG_NAME_FIELD_BOOSTS_SINK = "field.boost.sink";
	public static final String CONFIG_NAME_ONTOLOGY_BOOSTS_SINK = "ontology.boost.sink";
	public static final String CONFIG_NAME_MAX_CATEGOERY_LEVEL_EXCLUDE_SINK = "max_category_level_exclude.sink";
	public static final String CONFIG_NAME_CUTOFF_SCORE_SINK = "cutoff_score.sink";
	public static final String CONFIG_NAME_SCALE_FACTORS_SINK = "scale_factors.sink";
	
	/**
	 * @Constructor
	 * @param filePath is the class path to the config file
	 */
	protected ConfigProperties(String filePath) {
		InputStream inStream = ConfigProperties.class.getResourceAsStream(filePath);
		try {
			load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NullPointerException("Failed to load config file: " +
					filePath + ", error: " + e.getMessage());
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
				
		}
	}
	
	public int getInt(String propertyName, int defaultValue) {
		String valueStr = getProperty(propertyName);
		int propertyValue = defaultValue;		
		try {
			propertyValue = Integer.parseInt(valueStr);
		} catch (Exception e) {
			// do nothing, just return the default value;
		}
		
		return propertyValue;
	}
	
	public float getFloat(String propertyName, float defaultValue) {
		String valueStr = getProperty(propertyName);
		float propertyValue = defaultValue;		
		try {
			propertyValue = Float.parseFloat(valueStr);
		} catch (Exception e) {
			// do nothing, just return the default value;
		}
		
		return propertyValue;
	}
}
