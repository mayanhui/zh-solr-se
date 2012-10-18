package zh.solr.se.searcher.relevance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;

import zh.solr.se.searcher.solr.SolrUtil;

public class DocSliceResult extends ArrayList<ScoredSolrDoc> implements SearchResult {	
	private int matches = 0;

	/**
	 * construct an empty SearchResult object
	 */
	public DocSliceResult() {
	}
	
	/**
	 * Construct a SearchResult object from a DocSlice object
	 * @param docSlice the DocSlice object
	 */
	public DocSliceResult(DocSlice docSlice) {
		this(docSlice, null, null);
	}
	
	/**
	 * Construct a SearchResult object from a DocSlice object
	 * @param docSlice the DocSlice object
	 * @param searcher Solr index searcher
	 * @param fieldSet the desired field names and whether they multi-valued
	 */
	public DocSliceResult(DocSlice docSlice, SolrIndexSearcher searcher, Set<String> fieldSet) {
		if (docSlice == null)
			throw new NullPointerException("The DocSlice object for constructing search result must not be null");
		
		if (fieldSet != null && searcher == null) {
			throw new NullPointerException("Searcher must not be null to retieve field values");
		}
		
		// get the matches
		this.setMatches(docSlice.matches());
		
		// retrieve all the documents
		DocIterator iterator = docSlice.iterator();
		while (iterator.hasNext()) {
			int docId = iterator.nextDoc();
			float score = 1.0f;			
			if (docSlice.hasScores())
				score = iterator.score();
			
			ScoredSolrDoc doc = new ScoredSolrDoc(docId, score);
			
			// retrieve requested field values
			if (fieldSet != null) {
				Map<String, String> valueMap = SolrUtil.retrieveFieldValues(searcher, docId, fieldSet);
				doc.setFieldValueMap(valueMap);
			}
			
			this.add(doc);
		}
	}
	
	public void addDoc(Object doc) {
		if (doc == null)
			return;
		
		super.add((ScoredSolrDoc)doc);
		matches++;
	}
	
	/**
	 * Sort the doc list by score from high to low, and only return the specified number of docs
	 * @param maxCount max number of docs to return
	 */
	public void sortByScore(int maxCount) {
		if (this.size() < 2)
			return;
		
		// creating the comparator that compares doc scores from high to low
		Comparator<ScoredSolrDoc> comparator = new Comparator<ScoredSolrDoc>() {
			public int compare(ScoredSolrDoc doc1, ScoredSolrDoc doc2) {
				return (int)Math.signum(doc2.getScore() - doc1.getScore());
			}
		};
		
		if (maxCount <= 0 || maxCount >= this.size()) {
			// full sorting
			Collections.sort(this, comparator);
		} else {
			// use priority queue for partial sorting
			PriorityQueue<ScoredSolrDoc> docQueue = new PriorityQueue<ScoredSolrDoc>(maxCount, comparator);
			for (ScoredSolrDoc doc : this) {
				docQueue.add(doc);
			}
			
			// replace the original list with the items in the queue
			this.clear();
			for (int i = 0; i < maxCount; i++) {
				this.add(docQueue.poll());
			}
		}
	}
	
	/**
	 * Convert to a DocSlice object
	 */
	public DocSlice toSolrSearchResult() {
		int length = size();
		int[] idList = new int[length];
		float[] scoreList = new float[length];
		float maxScore = 0f;
		for (int i = 0; i < length; i++) {
			ScoredSolrDoc solrDoc = get(i);
			idList[i] = solrDoc.getDocId();
			scoreList[i] = solrDoc.getScore();
			if (scoreList[i] > maxScore)
				maxScore = scoreList[i];
		}
		
		return new DocSlice(0, length, idList, scoreList, matches, maxScore);
	}
	
	public SearchResult merge(SearchResult result1, SearchResult result2, int maxCount) {
		if (maxCount <= 0)
			maxCount = DEFAULT_PAGE_SIZE;
		
		if (result1 == null)
			return result2;
		else if (result2 == null || ((DocSliceResult)result1).size() >= maxCount)
			return result1;
				
		DocSliceResult mergedResult = new DocSliceResult();
		
		// append the first result to the merged result
		mergedResult.addAll((DocSliceResult)result1);
		
		// append the second result to the merged result, excluding any duplicates
		HashSet<Object> docIdHashSet = result1.getDocIdHashSet();
		int duplicates = 0;
		for (int i = 0; (i < ((DocSliceResult)result2).size() && mergedResult.size() <= maxCount); i++) {
			ScoredSolrDoc doc = ((DocSliceResult)result2).get(i);
			if (docIdHashSet.contains(doc.getDocId())) {
				duplicates++;
			} else {
				mergedResult.add(doc);
				docIdHashSet.add(doc.getDocId());
			}
		}
		
		// exclude the duplicates from the total matches
		int totalMatches = result1.getMatches() + result2.getMatches() - duplicates;
		mergedResult.setMatches(totalMatches);
		
		return mergedResult;
	}

	public void mergeWith(SearchResult anotherResult, int maxCount) {
		if (anotherResult == null || ((DocSliceResult)anotherResult).size() == 0)
			return;
		
		if (maxCount <= 0)
			maxCount = DEFAULT_PAGE_SIZE;
		
		int totalMatches = this.getMatches() + anotherResult.getMatches();
		if (maxCount <= this.size()) {
			// we need to cut the current result, and we don't need the second result.
			this.removeRange(maxCount, this.size());
		} else {
			// append needed docs
			HashSet<Object> docIdHashSet = this.getDocIdHashSet();
			int duplicates = 0;
			for (int i = 0; (i < ((DocSliceResult)anotherResult).size() && this.size() <= maxCount); i++) {
				ScoredSolrDoc doc = ((DocSliceResult)anotherResult).get(i);
				if (docIdHashSet.contains(doc.getDocId())) {
					duplicates++;
				} else {
					this.add(doc);
					docIdHashSet.add(doc.getDocId());
				}
			}
			
			// remove the duplicates from the total matches
			totalMatches -= duplicates;
		}
		
		setMatches(totalMatches);
	}
	
	public void scaleScores(float scaleFactor) {
		if (scaleFactor < 0)
			return;
		for (ScoredSolrDoc doc : this) {
			doc.setScore(doc.getScore() * scaleFactor);
		}
	}
	
	public void discardLowScoreResults(float threshold) {
		// cut from the end, so that the earlier index does not change after the cut
		// please note that the scores don't necessarily in any order
		int origSize = this.size();
		for (int i = origSize - 1; i >= 0; i--) {
			if (this.get(i).getScore() < threshold) {
				this.remove(i);
			}
		}
		
		// if any of the search result on the first page is removed, then the rest on later pages
		// should be removed too. We should update the matches to what are left on the first page
		if (this.size() < origSize)
			setMatches(this.size());
	}
	
	public int getMatches() {
		return matches;
	}

	public void setMatches(int matches) {
		this.matches = matches;
	}

	public Float getMaxScore() {
		float maxScore = 0f;
		for (ScoredSolrDoc doc : this) {
			float tempScore = doc.getScore();
			if (tempScore > maxScore)
				maxScore = tempScore;
		}
		
		return maxScore;
	}
	
	public HashSet<Object> getDocIdHashSet() {
		HashSet<Object> idSet = new HashSet<Object>();
		for (ScoredSolrDoc doc : this) {
			idSet.add(doc.getDocId());
		}
		
		return idSet;
	}
	
	public void removeRange (int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
	}

}
