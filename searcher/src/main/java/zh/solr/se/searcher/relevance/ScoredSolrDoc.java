package zh.solr.se.searcher.relevance;

import java.util.HashMap;
import java.util.Map;

public class ScoredSolrDoc {
	public static final String VALUE_SEPARATOR = "||";
	
	private int docId;
	private float score;
	private Map<String, String> fieldValueMap = new HashMap<String, String>();
	
	public ScoredSolrDoc(int docId, float score) {
		this.docId = docId;
		this.score = score;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	public void boostScore(float boost) {
		this.score += boost;
	}
	
	public String getFieldValue(String fieldName) {
		return fieldValueMap.get(fieldName);
	}
	
	public void setFeildValue(String fieldName, String fieldValue) {
		fieldValueMap.put(fieldName, fieldValue);
	}
	
	public Map<String, String> getFieldValueMap() {
		return fieldValueMap;
	}

	public void setFieldValueMap(Map<String, String> fieldValueMap) {
		if (fieldValueMap == null)
			this.fieldValueMap.clear();
		else
			this.fieldValueMap = fieldValueMap;
	}

	public String toString() {
		return "ID = " + docId + ", score = " + score;
	}
}
