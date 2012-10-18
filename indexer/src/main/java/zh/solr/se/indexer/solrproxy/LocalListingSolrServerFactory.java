package zh.solr.se.indexer.solrproxy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

import zh.solr.se.indexer.util.ConfigFactory;
import zh.solr.se.indexer.util.ConfigProperties;


/**
 * Factory class to fetch a LocalSolrServer This was created because I had
 * issues when running via hadoop in that using the environment settings to set
 * the home and config were causing an issue and I didn't have time to
 * investigate further.
 *
 */

public class LocalListingSolrServerFactory {
	private static LocalListingSolrServerFactory instance = new LocalListingSolrServerFactory();
	private CoreContainer coreContainer;
  private final Map<String, LocalSolrServer> serverMap = new HashMap<String, LocalSolrServer>();

	public static LocalListingSolrServerFactory getInstance() {
		return instance;
	}

	private LocalListingSolrServerFactory() {
		final Logger logger = Logger.getLogger("org.apache");
    logger.setLevel(Level.ALL);

		// set the Solr home
		final ConfigProperties config = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.SOLR_CONFIG_PATH);
		final String solrHome = config.getProperty(ConfigProperties.CONFIG_NAME_SOLR_HOME, "/var/solr/solr");
		System.setProperty("solr.solr.home", solrHome);

		// a workaround to keep the data import handlers happy since they use a relative path
    final String configFile = "/var/solr/solr/core-local/conf/data-config.xml";
		System.setProperty("data.import.config", configFile);

		// initialize the core container
		final CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		try {
			coreContainer = initializer.initialize();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new NullPointerException("Could not initialize Solr cores." +
					"Solr config might not been installed correctly. Error: " + e.getMessage());
		}

	}

	synchronized public LocalSolrServer getLocalSolrServer(String coreName) {
		// check for null core name, in case single core
		if (coreName == null) {
      coreName = "";
    }

		// check if the server instance already exists
		LocalSolrServer localServer = serverMap.get(coreName);
		if (localServer == null) {
			localServer = createLocalSolrServer(coreName);
			if (localServer != null) {
        serverMap.put(coreName, localServer);
      }
		}

		return localServer;
	}

	private LocalSolrServer createLocalSolrServer(final String coreName) {
		final SolrCore solrCore = coreContainer.getCore(coreName);
		final EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer, coreName);

		return new LocalSolrServer(solrServer, solrCore);
	}
}
