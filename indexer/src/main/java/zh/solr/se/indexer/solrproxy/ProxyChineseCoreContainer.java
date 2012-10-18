package zh.solr.se.indexer.solrproxy;

/**
 * Class used for a specific core. This simply overrides methods in the parent
 * to provide core specific values.
 * 
 */
public class ProxyChineseCoreContainer extends ProxyCoreContainer {
  @Override
  protected String getConfigFile() {
    return getSolrHome() + "/core-chinese/conf/data-config.xml";
  }

  @Override
  protected String getDataDir() {
    return getSolrHome() + "/core-chinese/data/";
  }
}
