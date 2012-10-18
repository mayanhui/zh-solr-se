package zh.solr.se.searcher.solr;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;

import zh.solr.se.searcher.relevance.DocSliceResult;
import zh.solr.se.searcher.relevance.ScoredSolrDoc;
import zh.solr.se.searcher.relevance.SearchResult;
import zh.solr.se.searcher.util.ConfigFactory;
import zh.solr.se.searcher.util.ConfigProperties;
import zh.solr.se.searcher.util.StringUtil;


public class SolrUtil {
	public static final String CORE_NAME_CHINESE = "core-chinese";

	public static final String INDEX_FIELD_MARKETING_TAG = "marketing_tag";
	// shared parameters
	public static final String PARAM_NAME_QUERY_TYPE   = "qt";
	public static final String PARAM_NAME_QUERY        = "q";
	public static final String PARAM_NAME_FILTER_QUERY = "fq";
	public static final String PARAM_NAME_FIELD_LIST   = "fl";

	// local search parameters
	public static final String PARAM_NAME_LOCATION   = "location";
	public static final String PARAM_NAME_LATITUDE   = "lat";
	public static final String PARAM_NAME_LONGITUDE  = "long";
	public static final String PARAM_NAME_RADIUS     = "radius";
	public static final String PARAM_NAME_ROWS       = "rows";

	// geoip search parameters
	public static final String PARAM_NAME_IP	=	"ip";
	public static final String PARAM_NAME_MODE	=	"mode";

	// local search parameters
	public static final String PARAM_NAME_DEBUG = "debug";

	// choose the algorithm
	public static final String PARAM_NAME_RELEVANCE_ALGORITHM = "relevance_algorithm";

	// Solr response field names
	public static final String SOLR_NAME_RESPONSE_HEADER = "responseHeader";
	public static final String SOLR_NAME_RESPONSE_PARAMS = "params";
	public static final String SOLR_NAME_RESPONSE_RESULT = "response";
	public static final String SOLR_NAME_RESPONSE_DOCS   = "docs";
	
	public static final String SOLR_NAME_FACET_COUNTS   = "facet_counts";
	public static final String SOLR_NAME_FACET_FIELDS   = "facet_fields";
	public static final String SOLR_NAME_FACET_QUERIES   = "facet_queries";
	
	
	public static final String SOLR_NAME_RESPONE_SOLR_SERVER_HOSTNAME = "solr_server_hostname";

	// parameter values
	public static final String PARAM_VALUE_SCORE      = "score";
	public static final String PARAM_VALUE_TRUE       = "true";
	public static final String PARAM_VALUE_ALGORITHM_EXACT = "exact_match";
	

	public static int getRows(SolrParams queryParams) {
		ConfigProperties searchProperties = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.SEARCH_CONFIG_PATH);
		int defaultRows = searchProperties.getInt(ConfigProperties.CONFIG_NAME_DEFAULT_ROWS, SearchResult.DEFAULT_PAGE_SIZE);
		int rows = queryParams.getInt(SolrUtil.PARAM_NAME_ROWS, defaultRows);

		return rows;
	}

	/**
	 * Add/modify/Remove a single parameter in the Solr request
	 * @param solrReq the Solr request
	 * @param name is the name of the parameter
	 * @param value is the parameter value to set. It will be removed from the parameter list if the value is null
	 */
	public static void setSolrRequestParam(SolrQueryRequest solrReq, String name, Object value) {
		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put(name, value);
		setSolrRequestParams(solrReq, valueMap);
	}

	/**
	 * Add/modify/Remove multiple parameters in the Solr request
	 * @param solrReq the Solr request
	 * @param valueMap a list of parameter name and value. A parameter will be removed if its value is null
	 */
	public static void setSolrRequestParams(SolrQueryRequest solrReq, Map<String, Object> valueMap) {
		if (valueMap == null)
			return;

		NamedList<Object> paramList = solrReq.getParams().toNamedList();
		for (String name : valueMap.keySet()) {
			setNamedListEntry(paramList, name, valueMap.get(name));
		}

		// create the new Solr request params and set it back to the request
		SolrParams params = SolrParams.toSolrParams(paramList);
		solrReq.setParams(params);
	}

	public static void setResponseHeaderParam(SolrQueryResponse solrResp, String name, Object value) {
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(name, value);
		setResponseHeaderParams(solrResp, paramMap);
	}

	public static void setResponseHeaderParams(SolrQueryResponse solrResp, Map<String, Object> paramMap) {
		if (solrResp == null || paramMap == null)
			return;

		NamedList responseValues = solrResp.getValues();
		NamedList header = (NamedList)responseValues.get(SOLR_NAME_RESPONSE_HEADER);
		if(header == null) {
			return;
		}

		NamedList headerParams = (NamedList)header.get(SOLR_NAME_RESPONSE_PARAMS);

		// update the parameters
		for (String name : paramMap.keySet()) {
			setNamedListEntry(headerParams, name, paramMap.get(name));
		}
	}

	public static void clearSolrResponse(SolrQueryResponse solrResp) {
		if (solrResp == null)
			return;

		NamedList responseValues = solrResp.getValues();
		if (responseValues == null)
			return;

		// remove the header
		setNamedListEntry(responseValues, SolrUtil.SOLR_NAME_RESPONSE_HEADER, null);

		// remove the search result
		setNamedListEntry(responseValues, SolrUtil.SOLR_NAME_RESPONSE_RESULT, null);
	}

	public DocSlice mergeSearchResultList(List<DocSlice> searchResultList) {
		return mergeSearchResultList(searchResultList, -1);
	}

	/**
	 * Merge two search results into one
	 *
	 * @param result1
	 * @param result2
	 * @param maxCount the max docs we need in the merged search result
	 * @return
	 */
	public static DocSlice mergeTwoSearchResults(DocSlice result1, DocSlice result2, int maxCount) {
		if (result1 == null)
			return result2;

		if (result2 == null)
			return result1;

		ArrayList<DocSlice> resultList = new ArrayList<DocSlice>();
		resultList.add(result1);
		resultList.add(result2);

		return mergeSearchResultList(resultList, maxCount);
	}

	/**
	 * Merge a list of DocSlice objects into one
	 * @param searchResultList
	 * @param maxCount the max docs we need in the merge search result
	 * @return
	 */
	public static DocSlice mergeSearchResultList(List<DocSlice> searchResultList, int maxCount) {
		if (searchResultList == null || searchResultList.size() == 0)
			return null;

		// if there is a valid max count, then cut the result to it if necessary
		int length = getTotalCount(searchResultList);
		if (maxCount > 0 && length > maxCount)
			length = maxCount;

		int[] docList = new int[length];
		float[] scoreList = new float[length];
		int count = 0;
		float maxScore = 0.0f;
		for (DocSlice searchResult : searchResultList) {
			if (searchResult == null)
				continue;

			// append the docs in the search result to the doc list
			count = appendDocs(searchResult, count, docList, scoreList, maxCount);

			// max score among all search results
			if (searchResult.hasScores()) {
				float tempMax = searchResult.maxScore();
				if (tempMax > maxScore)
					maxScore = tempMax;
			}

			if (maxCount > 0 && count >= maxCount)
				break;
		}

		// the difference between count and length is the number of duplicates
		int duplicates = length - count;

		int totalMatches = getTotalMatches(searchResultList) - duplicates;

		return new DocSlice(0, count, docList, scoreList, totalMatches, maxScore);
	}

	public static DocSlice cutoffLowScores(DocSlice searchResult, float cutoffScore) {
		if (searchResult == null || searchResult.size() <= 0 || !searchResult.hasScores() || cutoffScore <= 0)
			return searchResult;

		int length = searchResult.size();

		int[] docList = new int[length];
		float[] scoreList = new float[length];
		float maxScore = searchResult.maxScore();
		int matches = searchResult.matches();
		int count = 0;
		DocIterator iterator = searchResult.iterator();
		while (iterator.hasNext()) {
			int docId = iterator.nextDoc();
			float score = iterator.score();
			if (score >= cutoffScore) {
				// keep the search result only if its score is equal to or above the cutoff score
				docList[count] = docId;
				scoreList[count] = score;
				count++;
			}
		}

		if (count < length) {
			// if we already cut some from the first page, then total match should be what we keep
			matches = count;
		}

		return new DocSlice(0, count, docList, scoreList, matches, maxScore);
	}

	/**
	 * add/modify/remove the entries of a NamedList. See the method of setNamedLitEntry
	 * @param list
	 * @param valueMap
	 */
	public static void setNamedListEntries(NamedList list, Map<String, Object> valueMap) {
		if (list == null || valueMap == null)
			return;

		for (String name : valueMap.keySet()) {
			setNamedListEntry(list, name, valueMap.get(name));
		}
	}

	/**
	 * Add/modify/remove an entry of a named list. Add if the entry does not exist,
	 * modify if the entry exists, remove if the value is null
	 * @param namedList
	 * @param name
	 * @param value
	 */
	public static void setNamedListEntry(NamedList namedList, String name, Object value) {
		assert name != null;

		if(namedList == null) {
			return;
		}

		// remove the existing value
		Object existingValue = namedList.get(name);
		if (existingValue != null) {
			//namedList.remove(name);
			int idx = namedList.indexOf(name, 0);
			if (idx >= 0) {
			    namedList.remove(idx);
			}
		}

		// add the value
		if (value != null)
			namedList.add(name, value);
	}

	/**
	 * Get the total document count of the search result list.
	 * This is different from the total matches.
	 *
	 * @param searchResultList
	 * @return
	 */
	public static int getTotalCount(List<DocSlice> searchResultList) {
		if (searchResultList == null)
			return 0;

		int totalCount = 0;
		for (DocSlice searchResult : searchResultList) {
			totalCount += searchResult.size();
		}

		return totalCount;
	}

	/**
	 * Get the total matches of the search result list
	 *
	 * @param searchResultList
	 * @return
	 */
	public static int getTotalMatches(List<DocSlice> searchResultList) {
		if (searchResultList == null)
			return 0;

		int totalMatches = 0;
		for (DocSlice searchResult : searchResultList) {
			totalMatches += searchResult.matches();
		}

		return totalMatches;

	}

	/**
	 * Construct the search query against multiple fields with different boosts
	 *
	 * @param keyword the keyword to search for
	 * @param fieldsAndBoosts a list of field to search against and corresponding field boosts
	 * @return
	 */
	public static String constructQuery(String keyword, Map<String, Float> fieldsAndBoosts) {
		assert keyword != null;

		// we search the keywords against 3 fields, question_keyword,
		// question_tags, question with different boosts as configured
		StringBuilder queryBuilder = new StringBuilder("(");
		boolean firstField = true;
		for (String fieldName : fieldsAndBoosts.keySet()) {
			if (firstField)
				firstField = false;
			else
				queryBuilder.append(" OR ");
			queryBuilder.append(fieldName).append(":")
				.append("(").append(keyword).append(")^").append(fieldsAndBoosts.get(fieldName));
		}
		queryBuilder.append(")");

		return queryBuilder.toString();
	}

	/*
	 * Replace missing query with "q = *:*". Standard request handler would throw
	 * a NullPointerException if q is missing
	 *
	 * @param solrReq
	 */
	public static void fixMissingQuery(SolrQueryRequest solrReq) {
		String query = solrReq.getParams().get(SolrUtil.PARAM_NAME_QUERY);
		if (query == null || query.length() == 0)
			SolrUtil.setSolrRequestParam(solrReq, SolrUtil.PARAM_NAME_QUERY, "*:*");
	}

	/**
	 * Make sure the score field is included in the field list parameter
	 * @param solrReq the solr request to which we update the field list parameter
	 */
	public static void includeScoreInFieldList(SolrQueryRequest solrReq) {
		if (solrReq == null)
			return;

		String fieldList = solrReq.getParams().get(PARAM_NAME_FIELD_LIST);
		if (fieldList == null || fieldList.trim().length() == 0) {
			// the client did not specify field list, return all plus score
			fieldList = "*," + PARAM_VALUE_SCORE;
		} else if (fieldList.indexOf(PARAM_VALUE_SCORE) < 0){
			// the client specified a field list, but did not ask for score. add score to the list
			fieldList = fieldList + "," + PARAM_VALUE_SCORE;
		}

		// set the field list to the Solr request
		setSolrRequestParam(solrReq, PARAM_NAME_FIELD_LIST, fieldList);
	}

	/**
	 * Make sure the score and primary key field are included in the field list parameter
	 * @param solrReq the solr request to which we update the field list parameter
	 */
	public static void includeScoreAndKeyInFieldList(SolrQueryRequest solrReq, String uniqueKey) {
		if (solrReq == null)
			return;

		String fieldList = solrReq.getParams().get(PARAM_NAME_FIELD_LIST);
		if (fieldList == null || fieldList.trim().length() == 0) {
			// the client did not specify field list, return all plus score
			fieldList = "*," + PARAM_VALUE_SCORE;
		} else {
			if (fieldList.indexOf(uniqueKey) < 0){
				// the client specified a field list, but did not ask for key. add key to the list
				fieldList = fieldList + "," + uniqueKey;
			}

			if (fieldList.indexOf(PARAM_VALUE_SCORE) < 0){
				// the client specified a field list, but did not ask for score. add score to the list
				fieldList = fieldList + "," + PARAM_VALUE_SCORE;
			}
		}

		// set the field list to the Solr request
		setSolrRequestParam(solrReq, PARAM_NAME_FIELD_LIST, fieldList);
	}

	/**
	 * Check if the keyword is missing, or just white spaces, or just a parentheses
	 * @param keyword the keyword to check
	 * @return true if the keyword is empty
	 */
	public static boolean emptyKeyword(String keyword) {
		if (keyword == null)
			return true;

		keyword = keyword.trim();
		if (keyword.length() == 0)
			return true;

		// take off the opening and closing parenthesis
		if (keyword.startsWith("("))
			keyword = keyword.substring(1);
		if (keyword.endsWith(")"))
			keyword = keyword.substring(0, keyword.length() - 1);

		if (keyword.trim().length() == 0)
			return true;

		return false;
	}

	/**
	 * create a Solr query string in the form of "field:(value)^boost"
	 * @param fieldName
	 * @param fieldValue
	 * @param boost
	 * @return Solr field query
	 */
	public static String createFeildQuery(String fieldName, String fieldValue, float boost) {
		if (fieldName == null || fieldValue == null)
			return null;

		// escape the special characters in the field value
		fieldValue = QueryParser.escape(fieldValue);

		if (boost < 0)
			boost = 1.0f;

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(fieldName).append(":(").append(fieldValue).append(")^").append(boost);

		return queryBuilder.toString();
	}

	/**
	 * create a Solr query string in the form of "field:"keyword"^boost"
	 * @param fieldName
	 * @param fieldValue
	 * @param boost
	 * @return Solr field query
	 */
	public static String createExactMatchQuery(String fieldName, String fieldValue, float boost) {
		if (fieldName == null || fieldValue == null)
			return null;

		// escape the special characters in the field value
		fieldValue = QueryParser.escape(fieldValue);

		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(fieldName).append(":\"").append(fieldValue).append("\"");
		if (boost >= 0)
			queryBuilder.append("^").append(boost);

		return queryBuilder.toString();
	}

	/**
	 * For each document in the search result, retrieve the specified field values and populate the document with the values.
	 * @param searchResult
	 * @param searcher the solr index searcher for retrieving the field values
	 * @param fieldSet specifies the desired fields and whether they are mutliple valued.
	 */
	public static void populateFields(
			SearchResult searchResult, SolrIndexSearcher searcher, Set<String> fieldSet)
	{
		if (searchResult == null || searcher == null || fieldSet == null)
			return;

		for (ScoredSolrDoc doc : ((DocSliceResult)searchResult)) {
			doc.setFieldValueMap(retrieveFieldValues(searcher, doc.getDocId(), fieldSet));
		}
	}

	/**
	 * Retrieve the field values from a Solr document. The value is always a string.
	 * If there are multiple values, values will be put together as single string using the pre-defined separator.
	 *
	 * @param searcher the Solr searcher
	 * @param docId document ID of the document to retrieve field values from
	 * @param fieldNameSet a list of field names whose values are to be retrieved
	 * @return a map of field names and field values
	 */
	public static Map<String, String> retrieveFieldValues(
			SolrIndexSearcher searcher, int docId, Set<String> fieldNameSet)
	{
		assert searcher != null;
		if (fieldNameSet == null || fieldNameSet.size() == 0)
			return null;

		IndexSchema schema = searcher.getSchema();

		HashMap<String, String> fieldValueMap = new HashMap<String, String>();
		try {
			Document solrDoc = searcher.doc(docId, fieldNameSet);
			for (String fieldName : fieldNameSet) {
				String fieldValue = null;
				FieldType fieldType = schema.getField(fieldName).getType();

				// the following code is to make sure it works for sortable numerical fields
				if (fieldType.isMultiValued()) {
					// concatenate all the values for multi-valued field
					Fieldable[] fields = solrDoc.getFieldables(fieldName);
					if (fields != null && fields.length > 0) {
						ArrayList<String> valueList = new ArrayList<String>();
						for (Fieldable fd : fields) {
							Object fieldObj = fieldType.toObject(fd);
							if (fieldObj != null)
								valueList.add(fieldObj.toString());
						}
						fieldValue = StringUtil.listToString(valueList, ",");
					}

				} else {
					// single valued field
					Fieldable field = solrDoc.getFieldable(fieldName);
					if (field != null) {
						Object fieldObj = fieldType.toObject(field);
						if (fieldObj != null)
							fieldValue = fieldObj.toString();
					}
				}

				if (fieldValue != null)
					fieldValueMap.put(fieldName, fieldValue.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fieldValueMap;
	}

	/**
	 * Get the value of the specified Solr request parameter
	 * @param solrReq the Solr request object from which to retrieve the parameter
	 * @param paramName the name of the requested parameter
	 * @return the value of the parameter
	 */
	public static String getSolrRequestParameter(SolrQueryRequest solrReq, String paramName) {
		if (solrReq == null || paramName == null)
			return null;

		SolrParams params = solrReq.getParams();
		if (params == null)
			return null;

		return params.get(paramName);
	}

	/**
	 * Combine two Solr queries
	 * @param query1
	 * @param query2
	 * @return the combined query string
	 */
	public static String combineTwoQueries(String query1, String query2) {
		if (query1 == null)
			return query2;
		else if (query2 == null)
			return query1;
		else
			return "(" + query1 + " OR " + query2 + ")";
	}

	private static int appendDocs(DocSlice searchResult, int offset, int[] docList, float[] scoreList, int maxCount) {
		boolean hasScores = searchResult.hasScores();
		DocIterator iterator = searchResult.iterator();
		int count = offset;

		while (iterator.hasNext() && (maxCount <= 0 || count < maxCount)) {
			int docId = iterator.nextDoc();

			// don't add any duplicate documents
			if (docExists(docId, docList))
				continue;

			// add the doc to the total doc list
			docList[count] = docId;
			if (hasScores) {
				scoreList[count] = iterator.score();
			}
			count++;
		}

		return count;
	}

	private static boolean docExists(int docId, int[] docList) {
		// the assumption is the list is not too large, typically less than 10

		for (int existingId : docList) {
			if (docId == existingId)
				return true;
		}

		return false;
	}

	public static void printNamedList(NamedList<Object> valueList, String marker) {
		System.out.println("====================== " + marker + " Begins ====================== ");
		Iterator<Map.Entry<String, Object>> valueIterator = valueList.iterator();
		while (valueIterator.hasNext()) {
			Map.Entry<String, Object> entry = valueIterator.next();
			String name = entry.getKey();
			Object value = entry.getValue();
			System.out.println("name = " + name + ", value = " + value + ", type = " + value.getClass().getSimpleName());
		}
		System.out.println("====================== " + marker + " Ends ====================== ");
	}
	
	public static String getLocalIp() {
		String localIP = null;
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				if (null != ni) {
					Enumeration<InetAddress> ips = ni.getInetAddresses();
					while (ips.hasMoreElements()) {
						localIP = ips.nextElement().getHostAddress();
						if (localIP.startsWith("10.")) {
							return localIP;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return localIP;
	} 
	
	public static String getHostName() {
		String hostname = null;
		try {
			InetAddress add = InetAddress.getLocalHost();
			hostname = add.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return hostname;
	}

	public static void main(String[] args) {
	}
}
