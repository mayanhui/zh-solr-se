package zh.solr.se.searcher.relevance;

import java.util.HashSet;

public interface SearchResult{
	public static final int DEFAULT_PAGE_SIZE = 10;
	
	/**
	 * Sort the doc list by score from high to low, and only return the specified number of docs
	 * @param maxCount max number of docs to return
	 */
	public void sortByScore(int maxCount);
	
	/**
	 * Convert to a SolrSearchResult object
	 */
	public Object toSolrSearchResult();
	
	public SearchResult merge(SearchResult result1, SearchResult result2, int maxCount);

	public void mergeWith(SearchResult anotherResult, int maxCount);
	
	public void scaleScores(float scaleFactor);
	
	public void discardLowScoreResults(float threshold);
	
	public int getMatches();

	public void setMatches(int matches);

	public Float getMaxScore();
	
	public HashSet<Object> getDocIdHashSet();
	
	public void addDoc(Object doc);
	
	public void removeRange (int fromIndex, int toIndex);
	
	public int size();
}
