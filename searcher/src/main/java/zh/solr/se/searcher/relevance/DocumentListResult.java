package zh.solr.se.searcher.relevance;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class DocumentListResult extends SolrDocumentList implements SearchResult {
	private String uniqueKey;

	/**
	 * construct an empty SearchResult object
	 */
	public DocumentListResult(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}
	
	/**
	 * Construct a SearchResult object from a SolrDocumentList object
	 * @param solrDocumentList the SolrDocumentList object
	 * @param uniqueKey a unique key to get unique id
	 */
	public DocumentListResult(SolrDocumentList solrDocumentList, String uniqueKey) {
		if (solrDocumentList == null)
			throw new NullPointerException("The SolrDocumentList object for constructing search result must not be null");
		
		// get the matches
		this.setNumFound(solrDocumentList.getNumFound());
		this.setMaxScore(solrDocumentList.getMaxScore());
		this.setStart(solrDocumentList.getStart());
		this.uniqueKey = uniqueKey;
		this.addAll(solrDocumentList);
	}
	
	public void addDoc(Object doc) {
		if (doc == null)
			return;
		
		SolrDocument solrDoc = (SolrDocument)doc;
		
		// increment the number of found
		super.add(solrDoc);
		setNumFound(getNumFound() + 1);
		
		// update max score if necessry
		Object scoreObj = solrDoc.getFieldValue("score");
		if (scoreObj != null) {
			try {
				float score = Float.parseFloat(scoreObj.toString());
				if ( null==this.getMaxScore() || score > this.getMaxScore())
					this.setMaxScore(score);
			} catch (Exception e) {
				// ignore if score is not available
			}
		}
	}
	
	/**
	 * Sort the doc list by score from high to low, and only return the specified number of docs
	 * @param maxCount max number of docs to return
	 */
	public void sortByScore(int maxCount) {
		if (this.size() < 2)
			return;
		
		// creating the comparator that compares doc scores from high to low
		Comparator<SolrDocument> comparator = new Comparator<SolrDocument>() {
			public int compare(SolrDocument doc1, SolrDocument doc2) {
				float score1 = (Float)doc1.getFieldValue("score");
				float score2 = (Float)doc2.getFieldValue("score");
				return (int)Math.signum(score2 - score1);
			}
		};
		
		if (maxCount <= 0 || maxCount >= this.size()) {
			// full sorting
			Collections.sort(this, comparator);
		} else {
			// use priority queue for partial sorting
			PriorityQueue<SolrDocument> docQueue = new PriorityQueue<SolrDocument>(maxCount, comparator);
			for (SolrDocument doc : this) {
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
	public SolrDocumentList toSolrSearchResult() {		
		return this;
	}
	
	public SearchResult merge(SearchResult result1, SearchResult result2, int maxCount) {
		if (maxCount <= 0)
			maxCount = DEFAULT_PAGE_SIZE;
		
		if (result1 == null)
			return result2;
		else if (result2 == null || ((DocumentListResult)result1).size() >= maxCount)
			return result1;
				
		DocumentListResult mergedResult = new DocumentListResult(((DocumentListResult)result1).uniqueKey);
		
		// append the first result to the merged result
		mergedResult.addAll(((DocumentListResult)result1));
		
		// append the second result to the merged result, excluding any duplicates
		HashSet<Object> docIdHashSet = result1.getDocIdHashSet();
		int duplicates = 0;
		for (int i = 0; (i < ((DocumentListResult)result2).size() && mergedResult.size() <= maxCount); i++) {
			SolrDocument doc = ((DocumentListResult)result2).get(i);
			Object docId = doc.getFieldValue(uniqueKey);
			if (docIdHashSet.contains(docId)) {
				duplicates++;
			} else {
				mergedResult.add(doc);
				docIdHashSet.add(docId);
			}
		}
		
		// exclude the duplicates from the total matches
		int totalMatches = result1.getMatches() + result2.getMatches() - duplicates;
		mergedResult.setMatches(totalMatches);
		
		return mergedResult;
	}

	public void mergeWith(SearchResult anotherResult, int maxCount) {
		if (anotherResult == null || ((DocumentListResult)anotherResult).size() == 0)
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
			for (int i = 0; (i < ((DocumentListResult)anotherResult).size() && this.size() <= maxCount); i++) {
				SolrDocument doc = ((DocumentListResult)anotherResult).get(i);
				Object docId = doc.getFieldValue(uniqueKey);
				if (docIdHashSet.contains(docId)) {
					duplicates++;
				} else {
					this.add(doc);
					docIdHashSet.add(docId);
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
		for (SolrDocument doc : this) {
			float score = (Float)doc.getFieldValue("score");
			doc.put("score", score * scaleFactor);
		}
	}
	
	public void discardLowScoreResults(float threshold) {
		// cut from the end, so that the earlier index does not change after the cut
		// please note that the scores don't necessarily in any order
		int origSize = this.size();
		for (int i = origSize - 1; i >= 0; i--) {
			float score = (Float)this.get(i).getFieldValue("score");
			if (score < threshold) {
				this.remove(i);
			}
		}
		
		// if any of the search result on the first page is removed, then the rest on later pages
		// should be removed too. We should update the matches to what are left on the first page
		if (this.size() < origSize)
			setMatches(this.size());
	}
	
	public int getMatches() {
		return (int)getNumFound();
	}

	public void setMatches(int matches) {
		setNumFound(matches);
	}
		
	public HashSet<Object> getDocIdHashSet() {
		HashSet<Object> idSet = new HashSet<Object>();
		for (SolrDocument doc : this) {
			Object idObj = doc.getFieldValue(uniqueKey);			
			if (idObj != null)
				idSet.add(idObj);
			else
				throw new RuntimeException("Unique key: " + uniqueKey + ", must not be null");
		}
		
		return idSet;
	}
	
	public void removeRange (int fromIndex, int toIndex) {
		super.removeRange(fromIndex, toIndex);
	}

}
