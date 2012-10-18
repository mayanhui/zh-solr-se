package zh.solr.se.searcher;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.request.SolrRequestHandler;

import zh.solr.se.searcher.solr.SolrUtil;



public class DefaultSearchService {
	
	/**
	 * This is the default implementation of search service, i.e., no customization
	 * It behave exactly same as regular Solr server.
	 * @param req
	 * @param handler
	 * @param solrReq
	 * @param solrResp
	 */
	public void execute(HttpServletRequest req, SolrRequestHandler handler, 
			            SolrQueryRequest solrReq, SolrQueryResponse solrResp) 
	{
		doDefaultSearch(handler, solrReq, solrResp);
		
		//add solr_host_name to response
		String hostName = SolrUtil.getHostName();
		if(null != hostName)
			SolrUtil.setResponseHeaderParam(solrResp, SolrUtil.SOLR_NAME_RESPONE_SOLR_SERVER_HOSTNAME, hostName);
	}
	
	protected void doDefaultSearch(SolrRequestHandler handler, SolrQueryRequest solrReq, SolrQueryResponse solrResp) {
		// Don't make any change here!!! This has to be the default search behavior
		solrReq.getCore().execute(handler, solrReq, solrResp);
	}
		
	/**
	 * Do a Solr query using the give query string
	 * @param handler Solr handler to use
	 * @param solrReq Solr request, need to override the query parameter with the give query string
	 * @param solrResp Solr response to use
	 * @param query the Solr query string to use
	 * @return the result object, which could be DocSlice or SolrDocumentSet
	 */
	protected Object doSolrQuery(SolrRequestHandler handler, SolrQueryRequest solrReq, 
			SolrQueryResponse solrResp, String query)
	{
		if (query == null || query.length() == 0)
			return null;
		
		SolrUtil.setSolrRequestParam(solrReq, SolrUtil.PARAM_NAME_QUERY, query);
		doDefaultSearch(handler, solrReq, solrResp);
		
		return solrResp.getValues().get(SolrUtil.SOLR_NAME_RESPONSE_RESULT);
	}
}
