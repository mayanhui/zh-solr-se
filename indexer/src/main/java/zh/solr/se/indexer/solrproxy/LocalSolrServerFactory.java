package zh.solr.se.indexer.solrproxy;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalSolrServerFactory {
	private static LocalSolrServerFactory instance = new LocalSolrServerFactory();
	private CoreContainer coreContainer;
  // Hashtable is synchronized.  HashMap is not.
  private final Map<String, LocalSolrServer> serverMap = new Hashtable<String, LocalSolrServer>();
  private final Logger logger;

  /**
   * Do not allow instantiation of this class directly.
   */
  private LocalSolrServerFactory() {
    logger = Logger.getLogger("solrproxy");
    logger.setLevel(Level.SEVERE);
  }

  /**
   * Get THE instance of this class.
   *
   * @return THE instance
   */
	public static LocalSolrServerFactory getInstance() {
		return instance;
	}

  /**
   * Retrieve a local solr server (they are stored in a Map so this will always
   * return the same solr server for each core)
   *
   * @param coreName name of the core
   * @return the single solr server
   * @throws UnsupportedCoreException
   */
  synchronized public LocalSolrServer getLocalSolrServer(String coreName) throws UnsupportedCoreException {
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

  /**
   * Call close() on the specified server.  Remove the server from the map so
   * that all references to the server return to 0 and GC can clean it up.
   *
   * @param coreName the name of the core
   * @throws Exception
   */
  synchronized public void closeSolrServer(String coreName) throws Exception {
    LocalSolrServer solrServer = getLocalSolrServer(coreName);
    if(solrServer != null) {
      solrServer.close();
      removeSolrServerFromMap(coreName);
      solrServer = null;
    }
  }

  /**
   * Remove the server from the Map so references to the server return to 0 and
   * the GC can clean it up.  Shutdown the core container.
   *
   * @param coreName the name of the core.
   * @throws UnsupportedCoreException
   */
  synchronized private void removeSolrServerFromMap(String coreName) throws UnsupportedCoreException {
    logger.log(Level.INFO, "Removing solr server '" + coreName + "' from map [size:" + serverMap.size() + "] [" + getListOfCores() + "]");
    LocalSolrServer solrServer = getLocalSolrServer(coreName);
    if(solrServer == null) {
      logger.log(Level.INFO, "Solr server '" + coreName + "' not found in map [size:" + serverMap.size() + "] [" + getListOfCores() + "]");
    }
    else {
      if(serverMap.remove(coreName) == null) {
        logger.log(Level.SEVERE, "Remove returned nothing.  Failed removing solr server '" + coreName + "' from map [size:" + serverMap.size() + "] [" + getListOfCores() + "]");
      }
      logger.log(Level.INFO, "Shutting down solr container for core '" + coreName);
      ProxyCoreContainerFactory.getInstance().shutdownCoreContainer(coreName);
    }
    logger.log(Level.INFO, "Done Removing solr server '" + coreName + "' from map");
  }

  /**
   * Return a comma-delimited list of core names currently in the map.  For
   * logging purposes primarily.
   *
   * @return list of core names
   */
  private String getListOfCores() {
    StringBuilder cores = new StringBuilder();
    if(serverMap != null && serverMap.size() > 0) {
      Set keys = serverMap.keySet();
      Iterator iter = keys.iterator();
      while(iter.hasNext()) {
        if(cores.length() > 0) {
          cores.append(", ");
        }
        cores.append(iter.next());
      }
    }
    return cores.toString();
  }

  /**
   * Create a local solr server.
   *
   * @param coreName the core name
   * @return the local solr server
   * @throws UnsupportedCoreException
   */
  private LocalSolrServer createLocalSolrServer(final String coreName) throws UnsupportedCoreException {
    logger.log(Level.INFO, "Creating local solr server '" + coreName + "'");
    final SolrCore solrCore = getCoreContainer(coreName).getCore(coreName);
    final EmbeddedSolrServer solrServer = new EmbeddedSolrServer(coreContainer, coreName);

    logger.log(Level.INFO, "Created local solr server '" + coreName + "'");
    return new LocalSolrServer(solrServer, solrCore);
  }

  /**
   * Return the core container
   *
   * @param coreName the core name
   * @return the container
   * @throws UnsupportedCoreException
   */
	private CoreContainer getCoreContainer(final String coreName) throws UnsupportedCoreException {
		final ProxyCoreContainerFactory instance = ProxyCoreContainerFactory.getInstance();
		// REMEMBER TO SET THIS VALUE ON THE INSTANCE VARIABLE OR YOU'LL HAVE A
		// PROBLEM
		// No index will be created and it will be difficult to track down the cause
		coreContainer = instance.getCoreContainer(coreName);
		return coreContainer;
	}
}
