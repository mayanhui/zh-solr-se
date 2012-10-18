package zh.solr.se.indexer.solrproxy;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.core.CoreContainer;

import zh.solr.se.indexer.util.ConfigFactory;
import zh.solr.se.indexer.util.ConfigProperties;


/**
 * Abstract class used to get a CoreContainer. The sub-classes are created for
 * specific cores.
 *
 */
public abstract class ProxyCoreContainer {
  protected static final String DEFAULT_SOLR_HOME = "/var/solr/solr";

  private ConfigProperties configProperties;

  public ProxyCoreContainer() {
    configProperties = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.SOLR_CONFIG_PATH);
  }

  /**
   * Provide the path to the core's configuration file. This is core specific.
   *
   * @return
   */
  abstract protected String getConfigFile();

  /**
   * Provide the path to the core's data directory. This is core specific.
   *
   * @return
   */
  abstract protected String getDataDir();

  /**
   * Returns the configured value for the SOLR home directory
   *
   * @return
   */
  protected String getSolrHome() {
    return getConfigProperties().getProperty(ConfigProperties.CONFIG_NAME_SOLR_HOME, DEFAULT_SOLR_HOME);
  }

  /**
   * Can be used here or by sub-classes for access to the configuration data
   *
   * @return
   */
  protected ConfigProperties getConfigProperties() {
    if(configProperties == null) {
      configProperties = ConfigFactory.getInstance().getConfigProperties(ConfigFactory.SOLR_CONFIG_PATH);
    }

    return configProperties;
  }

  /**
   * Fetch the specific CoreContainer
   *
   * @return
   */
  public CoreContainer getContainer() {
    final Logger logger = Logger.getLogger("org.apache");
    logger.setLevel(Level.SEVERE);

    // set the Solr home
    System.setProperty("solr.solr.home", getSolrHome());

    // // a workaround to keep the data import handlers happy since they use a
    // // relative path
    // System.setProperty("data.import.config", getConfigFile());
    //
    // // a workaround to keep the data import handlers happy since they use a
    // // relative path
    // System.setProperty("solr.data.dir", getDataDir());

    // initialize the core container
    CoreContainer coreContainer = null;
    final CoreContainer.Initializer initializer = new CoreContainer.Initializer();
    try {
      // coreContainer.load(getSolrHome(), solrXml);
      coreContainer = initializer.initialize();
    }
    catch(final Exception e) {
      e.printStackTrace();
      throw new NullPointerException("Could not initialize Solr cores."
          + "Solr configProperties might not been installed correctly. Error: " + e.getMessage());
    }
    return coreContainer;
  }
}
