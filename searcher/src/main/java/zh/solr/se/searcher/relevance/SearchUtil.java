package zh.solr.se.searcher.relevance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zh.solr.se.searcher.solr.SolrUtil;
import zh.solr.se.searcher.util.ConfigFactory;
import zh.solr.se.searcher.util.ConfigProperties;
import zh.solr.se.searcher.util.StringUtil;

public class SearchUtil {
	
	/**
	 * Read the configured field boosts from config file
	 * @param boostsFieldName different core had different boosts, and they are configured under different field name
	 * @return the boosts as a map with the Solr field name as the key and the boost as the value
	 */
	public static Map<String, Float> getFieldBoostsFromConfig(String boostsFieldName) {
		ConfigProperties searchProperties = ConfigFactory.getInstance()
					.getConfigProperties(ConfigFactory.SEARCH_CONFIG_PATH);
		String boostsStr = searchProperties.getProperty(boostsFieldName);
		
		return parseFieldBoosts(boostsStr);
	}
	
	public static Map<String, Float> parseFieldBoosts(String boostsStr) {
		if (boostsStr == null || boostsStr.length() < 3)
			return null;
		
		// the field boosts are in the format of "field1:boost1,field2:boost2,field3:boost3..."
		ArrayList<String> fieldList = StringUtil.stringToStringList(boostsStr, 
				StringUtil.getRegExDelimiter(ConfigProperties.FIELD_SEPARATOR));
		if (fieldList == null || fieldList.size() == 0)
			return null;
		
		HashMap<String, Float> boostMap = new HashMap<String, Float>();
		for (String fieldStr : fieldList) {
			String[] nameValuePair = fieldStr.split(StringUtil.getRegExDelimiter(ConfigProperties.NAME_VALUE_SEPARATOR));
			if (nameValuePair != null && nameValuePair.length == 2) {
				try {
					float boost = Float.parseFloat(nameValuePair[1].trim());
					if (boost > 0)
						boostMap.put(nameValuePair[0].trim(), boost);
				} catch (Exception e) {
					// skip this entry
				}
			}
		}
		
		return boostMap;
	}
	
	public static List<Float> getScaleFactorsFromConfig(String factorsFieldName) {
		ConfigProperties searchProperties = ConfigFactory.getInstance()
			.getConfigProperties(ConfigFactory.SEARCH_CONFIG_PATH);
		String factorsStr = searchProperties.getProperty(factorsFieldName);
		
		return parseScaleFactors(factorsStr);
	}
	
	public static List<Float> parseScaleFactors(String factorsStr) {
		if (factorsStr == null)
			return null;

		// the field boosts are in the format of "field1:boost1,field2:boost2,field3:boost3..."
		ArrayList<String> factorStrList = StringUtil.stringToStringList(factorsStr, 
			StringUtil.getRegExDelimiter(ConfigProperties.FIELD_SEPARATOR));
		if (factorStrList == null || factorStrList.size() == 0)
			return null;

		ArrayList<Float> factorValueList = new ArrayList<Float>();
		for (String factorStr : factorStrList) {
			float factorValue = 1.0f;
			try {
				factorValue = Float.valueOf(factorStr.trim());
			} catch (Exception e) {
				// do nothing, use the default value
			}
			factorValueList.add(factorValue);
		}

		return factorValueList;	
	}

	/**
	 * Boost a document if its marketing tag matches the keyword
	 * Sort the document list with the new scores.
	 * @param searchResult the list of documents
	 * @param solrResp Solr response object that holds the search results
	 * @param keyword to be matched against marketing tag of the each document
	 * @param maxCount the wanted number of documents in the search result
	 * @return DocSlice with the documents sorted with the new relevance scores
	 */
	public static void sortSearchResultByMarketingTagMatch(
			SearchResult searchResult, String keyword, int maxCount) 
	{
		if (searchResult == null || keyword == null)
			return;
				
		// boost documents whose marketing_tag matches the keyword with the current maximum score
		for (ScoredSolrDoc doc : (DocSliceResult)searchResult) {
			boostByMarketingTagMatch(doc, keyword, searchResult.getMaxScore());
		}
		
		// sort by the new scores, and we only want to top N documents, and N = maxCount
		searchResult.sortByScore(maxCount);		
	}

	
	public static void boostByMarketingTagMatch(ScoredSolrDoc doc, String keyword, float boost) {
		if (doc == null || keyword == null)
			return;
		
		if (boost <= 0)
			boost = 5.0f;
		
		List<String> keywordList = StringUtil.stringToStringList(keyword, "[ ]");
		int keywordCount = keywordList.size();
		
		// to keep the maximum boost = boost, scale down the boost by number of words in the keyword
		if (keywordCount > 0)
			boost /= (float)keywordCount;
		else
			return;
		
		String marketingTag = doc.getFieldValue(SolrUtil.INDEX_FIELD_MARKETING_TAG);
		if (marketingTag == null || marketingTag.length() == 0)
			return;
		
		List<String> tagList = StringUtil.stringToStringList(marketingTag, "[ ]");
		int tagCount = tagList.size();
		
		// compare the last words between marketing tag and keywords
		int compareSize = (keywordCount < tagCount) ? keywordCount : tagCount;
		
		for (int i = 0; i < compareSize; i++) {
			if (StringUtil.wordMatch(keywordList.get(keywordCount - 1 - i), tagList.get(tagCount - 1 - i))) {
				doc.boostScore(boost);
			} else {
				// do not continue matching if a non-match already found
				break;
			}
		}
	}
}
