package zh.solr.se.searcher.solr;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.core.CoreContainer;

import zh.solr.se.searcher.util.ConfigFactory;
import zh.solr.se.searcher.util.ConfigProperties;


public class SolrProxy {
	protected static CoreContainer coreContainer;
	
	// Solr Server is a single instance for each core
	protected SolrServer solrServer;
	
	// load the cores once
	static {
	    try {
			ConfigProperties config = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.SEARCH_CONFIG_PATH);
			String solrHomePath = config.getProperty("solr.solr.home");
		    File solrHomeFile = new File( solrHomePath);
		    File coreConfigFile = new File(solrHomeFile, "solr.xml");
		    coreContainer = new CoreContainer();
		    coreContainer.load(solrHomePath, coreConfigFile);
	    } catch (Throwable t) {
	    	// this should not happen. If it does, the solr core config file has problem
	    	throw new RuntimeException("Solr core config file, solr.xml, is not properly installed. Error: " +
	    			t.getMessage());
	    }
	}
	
	/**
	 * @Constructor
	 * construct a proxy to a local Solr server
	 * @param coreName the Solr core name
	 */
	public SolrProxy(String coreName) {
		solrServer = new EmbeddedSolrServer(coreContainer, coreName);
	}
	
	/**
	 * @Constructor
	 * construct a proxy to a remote Solr server
	 * @param hostName
	 * @param coreName
	 * @throws MalformedURLException 
	 */
	public SolrProxy(String hostName, String coreName) {
		if (hostName == null)
			hostName = "localhost";
		
		String url = "http://" + hostName + ":8983/solr/" + coreName;
		try {
			solrServer = new CommonsHttpSolrServer( url );
		} catch (MalformedURLException e) {
			// this should not happen
			throw new RuntimeException("Invalid URL: " + url + ". Error: " + e.getMessage());
		}
	}
}
