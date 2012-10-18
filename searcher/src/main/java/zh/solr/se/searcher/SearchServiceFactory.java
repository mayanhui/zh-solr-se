package zh.solr.se.searcher;

import zh.solr.se.searcher.chinese.ChineseSearchService;
import zh.solr.se.searcher.solr.SolrUtil;

public class SearchServiceFactory {
	public static DefaultSearchService getSearchService(String coreName) {
		// create a new instance of handler for each request
		if (coreName == null)
			return new DefaultSearchService();

		if (coreName.equals(SolrUtil.CORE_NAME_CHINESE)) {
			return new ChineseSearchService();
		} else {
			return new DefaultSearchService();
		}
	}
}
