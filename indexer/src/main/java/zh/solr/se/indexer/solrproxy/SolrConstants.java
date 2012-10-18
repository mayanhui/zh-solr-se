package zh.solr.se.indexer.solrproxy;

public interface SolrConstants {
	public static final String CORE_NAME_CHINESE = "core-chinese";
	// local search parameters
	public static final String PARAM_NAME_LOCATION = "location";
	public static final String PARAM_NAME_LATITUDE = "lat";
	public static final String PARAM_NAME_LONGITUDE = "long";
	public static final String PARAM_NAME_RADIUS = "radius";

	// Solr response field names
	public static final String SOLR_NAME_RESPONSE_HEADER = "responseHeader";
	public static final String SOLR_NAME_RESPONSE_PARAMS = "params";
	public static final String SOLR_NAME_RESPONSE_RESULT = "response";
	public static final String SOLR_NAME_RESPONSE_DOCS = "docs";

	// index field names
	public static final String FIELD_NAME_SCORE = "score";

	// index fields for chinese
	public static final String FIELD_CHINESE_ID = "chinese_id";
	public static final String FIELD_CHINESE_NAME = "chinese_name";
	public static final String FIELD_CHINESE_CONTENT = "chinese_content";

	// field used for hadoop
	// the idea is that the value of this key will be used to name the directory
	// where the SOLR index is written
	public static final String FIELD_MAP_TASK_ID = "map_task_id";

}
