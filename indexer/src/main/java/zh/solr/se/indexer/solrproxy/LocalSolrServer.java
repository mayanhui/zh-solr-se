package zh.solr.se.indexer.solrproxy;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalSolrServer {
	private final EmbeddedSolrServer solrServer;
	private final SolrCore solrCore;
	private static Logger logger = Logger.getLogger("LocalSolrServer");

	protected LocalSolrServer(final EmbeddedSolrServer solrServer,
			final SolrCore solrCore) {
		this.solrServer = solrServer;
		this.solrCore = solrCore;

		logger.setLevel(Level.ALL);
	}

	/**
	 * Add a Solr document to the index
	 * 
	 * @param solrDoc
	 * @throws Exception
	 *             when an I/O problem occurs
	 */
	public void addDocument(final SolrInputDocument solrDoc) throws Exception {
		solrServer.add(solrDoc);
	}

	/**
	 * Optimize the index to 1 index file after indexing is done
	 * 
	 * @throws Exception
	 *             when an I/O problem occurs
	 */
	public void optimize() throws Exception {
		if (solrServer != null) {
			logger.log(Level.INFO, "Optimizing solr server");
			solrServer.optimize(false, true, 1);
		}
	}

	/**
	 * commit the changes to index
	 * 
	 * @throws Exception
	 *             if there is an IO problem
	 */
	public void commit() throws Exception {
		if (solrServer != null) {
			logger.log(Level.INFO, "Committing solr server");
			solrServer.commit();
		}
	}

	public void forceCommit() throws Exception {
		logger.log(Level.INFO, "Force commit solr server");
		final UpdateRequest req = new UpdateRequest();
		req.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, false);
		final UpdateResponse rsp = req.process(solrServer);
	}

	public void close() {
		if (solrServer != null) {
			logger.log(Level.INFO, "Closing solr server");
			solrCore.close();
		}
	}

	public static void main(final String[] args)
			throws UnsupportedCoreException {
		final LocalSolrServer proxy = LocalSolrServerFactory.getInstance()
				.getLocalSolrServer("core-qa");
		proxy.close();
	}
}
