package zh.solr.se.searcher;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.servlet.SolrDispatchFilter;


public class MainDispatchFilter extends SolrDispatchFilter {
	protected void execute(HttpServletRequest req, SolrRequestHandler handler,
			SolrQueryRequest solrReq, SolrQueryResponse solrResp) {
		solrReq.getContext().put("webapp", req.getContextPath());
		if (SearchHandler.class.isAssignableFrom(handler.getClass())) {
			// delegate to the right search service
			String coreName = solrReq.getCore().getName();
			DefaultSearchService service = SearchServiceFactory
					.getSearchService(coreName);
			service.execute(req, handler, solrReq, solrResp);
		} else
		{
			super.execute(req, handler, solrReq, solrResp);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
	}
}
