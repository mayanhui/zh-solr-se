package zh.solr.se.indexer.solrproxy;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

public class RemoteSolrProxy extends CommonsHttpSolrServer {
	private String hostName;
	private String coreName;
	
	public RemoteSolrProxy(String hostName, String coreName) throws Exception {
		super("http://" + hostName + ":8983/solr/" + coreName);
		this.hostName = hostName;
		this.coreName = coreName;
	}
	
	public SolrDocumentList search(Map<String, String> paramMap) throws Exception {
		assert (paramMap != null && paramMap.size() > 0);
		
		SolrQuery query = constructQuery(paramMap);
		SolrResponse solrResp = query(query);
		if (solrResp == null)
			return null;
		
		NamedList<Object> responseFeilds = solrResp.getResponse();
		if (responseFeilds == null)
			return null;
		
		SolrDocumentList docList = (SolrDocumentList)responseFeilds.get(SolrConstants.SOLR_NAME_RESPONSE_RESULT);
		
		return docList;
	}
	
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getCoreName() {
		return coreName;
	}

	public void setCoreName(String coreName) {
		this.coreName = coreName;
	}

	private SolrQuery constructQuery(Map<String, String> paramMap) {
		SolrQuery query = new SolrQuery();
		for (String paramName : paramMap.keySet()) {
			query.setParam(paramName, paramMap.get(paramName));
		}
				
		return query;
	}
	
	public static void main(String[] args) {
	}
}
