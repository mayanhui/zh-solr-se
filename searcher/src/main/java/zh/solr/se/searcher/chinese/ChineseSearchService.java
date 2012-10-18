package zh.solr.se.searcher.chinese;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.request.SolrRequestHandler;

import zh.solr.se.searcher.DefaultSearchService;
import zh.solr.se.searcher.solr.SolrUtil;

import javax.servlet.http.HttpServletRequest;

public class ChineseSearchService extends DefaultSearchService {

	@Override
	public void execute(HttpServletRequest req, SolrRequestHandler handler,
			SolrQueryRequest solrReq, SolrQueryResponse solrResp) {
		// execute the search
		doDefaultSearch(handler, solrReq, solrResp);

		// add solr_host_name to response
		String hostName = SolrUtil.getHostName();
		if (null != hostName)
			SolrUtil.setResponseHeaderParam(solrResp,
					SolrUtil.SOLR_NAME_RESPONE_SOLR_SERVER_HOSTNAME, hostName);
	}

}
