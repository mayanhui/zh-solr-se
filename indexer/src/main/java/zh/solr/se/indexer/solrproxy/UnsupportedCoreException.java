package zh.solr.se.indexer.solrproxy;

public class UnsupportedCoreException extends Exception {
	public UnsupportedCoreException(String coreName) {
		super("We don't have an indexer for the specified core: " + coreName);
	}
}
