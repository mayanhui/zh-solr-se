package zh.solr.se.indexer.solrproxy;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.core.CoreContainer;

/**
 * A Factory class that will provide a CoreContainer specific to a given core.
 *
 */
public class ProxyCoreContainerFactory {
  private static ProxyCoreContainerFactory instance = new ProxyCoreContainerFactory();

  // Hashtable is synchronized.  HashMap is not.
  private final Map<String, CoreContainer> coreContainerMap = new Hashtable<String, CoreContainer>();
  private final Logger logger;

  /**
   * Allow nothing to instantiate this class directly.
   */
  private ProxyCoreContainerFactory() {
    logger = Logger.getLogger("solrproxy");
    logger.setLevel(Level.SEVERE);
  }

  /**
   * Get THE instance of this class
   *
   * @return THE instance
   */
  public static ProxyCoreContainerFactory getInstance() {
    return instance;
  }

  /**
   * Fetch a CoreContainer specifically configured for a given core.
   *
   * @param coreName
   * @return
   * @throws UnsupportedCoreException
   */
  synchronized public CoreContainer getCoreContainer(final String coreName) throws UnsupportedCoreException {
    logger.log(Level.INFO, "Getting core container '" + coreName + "'");
    // check if the instance already exists
    CoreContainer container = coreContainerMap.get(standardizeCoreName(coreName));
    if(container == null) {
      container = createCoreContainer(coreName);
      if(container != null) {
        coreContainerMap.put(coreName, container);
      }
    }

    if(container == null) {
      logger.log(Level.INFO, "Could not get core container '" + coreName + "'");
    }
    else {
      logger.log(Level.INFO, "Got core container '" + coreName + "'");
    }
    return container;
  }

  /**
   * Call shutdown() on the specified core and remove it from the map so that
   * all references to the core will be set to zero and GC can clean it up.
   *
   * @param coreName the name of the core
   * @throws UnsupportedCoreException
   */
  synchronized public void shutdownCoreContainer(String coreName) throws UnsupportedCoreException {
    logger.log(Level.INFO, "Start shutting down core container '" + coreName + "'");
    CoreContainer container = getCoreContainer(coreName);
    if(container != null) {
      container.shutdown();
      logger.log(Level.INFO, "Core container '" + coreName + "' has been shutdown.");
      removeContainerFromMap(coreName);
    }
  }

  /**
   * So that all references to the specific core are set to zero for GC, remove
   * the core from the map
   * @param coreName
   * @throws UnsupportedCoreException
   */
  synchronized private void removeContainerFromMap(String coreName) throws UnsupportedCoreException {
    logger.log(Level.INFO, "Removing core container '" + coreName + "' from map [size:" + coreContainerMap.size() + "]");
    CoreContainer container = getCoreContainer(coreName);
    if(container == null) {
      logger.log(Level.SEVERE, "Could not get the core container object '" + coreName + "' from map [size:" + coreContainerMap.size() + "]");
    }
    else {
      if(coreContainerMap.remove(coreName) == null) {
        logger.log(Level.INFO, "Remove returned nothing.  Could not remove core container '" + coreName + "' from map [size:" + coreContainerMap.size() + "]");
      }
      else {
        logger.log(Level.INFO, "Removed core container '" + coreName + "' from map [size:" + coreContainerMap.size() + "]");
      }
    }
  }

  /**
   * Here's where the factory part takes place. See if a CoreContainer has
   * already been instantiated and if so return it. Otherwise, create a new one
   * and return it.
   *
   * @param coreName the name of the core
   * @return a core container
   * @throws UnsupportedCoreException
   */
  private CoreContainer createCoreContainer(final String coreName) throws UnsupportedCoreException {
    final String standardizedCoreName = standardizeCoreName(coreName);
    CoreContainer coreContainer = null;

    if(standardizedCoreName.equalsIgnoreCase(SolrConstants.CORE_NAME_CHINESE)) {
      coreContainer = new ProxyChineseCoreContainer().getContainer();
    }else {
      throw new UnsupportedCoreException(coreName);
    }

    return coreContainer;
  }

  /**
   * The core name could come in a "qa" or "core-qa". This method standardizes
   * the name to begin with "core-" so that the rest of the code can be sure
   * it's working with a known format.
   *
   * @param coreName
   * @return
   */
  private String standardizeCoreName(String coreName) {
    if((coreName != null) && !coreName.startsWith("core-")) {
      coreName = "core-" + coreName.trim().toLowerCase();
    }
    return coreName;
  }

}
