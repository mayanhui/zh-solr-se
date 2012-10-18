package zh.solr.se.indexer.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IndexerUtil {
	public static final String OPTION_SEPARATOR = "#";
	private static final String BOOST_SEPARATOR = ",";
	private static final String NAME_VALUE_SEPARATOR = ":";
	
	/**
	 *  load the field boosts from config file
	 * @param fieldBootsPropertyName the property name in the config file
	 * @return field boosts as a map
	 */
	public static Map<String, Float> readFieldBoostsFromConfig(String fieldBootsPropertyName) {
		ConfigProperties config = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.INDEXER_CONFIG_PATH);
		String boostsStr = config.getProperty(fieldBootsPropertyName);
		Map<String, Float> fieldBoostMap = new HashMap<String, Float>();
		if (boostsStr != null) {
			// The format of field boosts is "field1:value1,field2:value2,..."
			List<String> boostList = StringUtil.stringToStringList(
					boostsStr, StringUtil.getRegExDelimiter(BOOST_SEPARATOR));
			if (boostList != null && boostList.size() > 0) {
				for (String fieldBoost : boostList) {
					List<String> values = StringUtil.stringToStringList(
							fieldBoost, StringUtil.getRegExDelimiter(NAME_VALUE_SEPARATOR));
					if (values != null && values.size() == 2) {
						fieldBoostMap.put(values.get(0), Float.valueOf(values.get(1)));
					}
				}
			}
		}
		
		return fieldBoostMap;
	}

	/*
	 * Recursively delete all files in the directory
	 */
	public static void deleteFilesInDir(File dir, boolean deleteItself) {
		if (dir == null)
			return;
		
		// if the file is directory, delete the contained files first
		if (dir.isDirectory()) {
			File[] childrenFiles = dir.listFiles();
			if (childrenFiles != null && childrenFiles.length > 0) {
				for (File childFile : childrenFiles) {
					deleteFilesInDir(childFile, true);
				}
			}
		}
		
		// delete the dir/file itself
		if (deleteItself)
			dir.delete();
	}
}
