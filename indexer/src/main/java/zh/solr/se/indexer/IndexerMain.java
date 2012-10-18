package zh.solr.se.indexer;

import zh.solr.se.indexer.chinese.ChineseIndexer;
import zh.solr.se.indexer.solrproxy.SolrConstants;
import zh.solr.se.indexer.solrproxy.UnsupportedCoreException;

public class IndexerMain {
	private void run(final String coreName, String dataType, String sourceFile)
			throws Exception {
		final IndexerBase indexer = getIndexer(coreName, dataType, sourceFile);
		if (indexer != null) {
			indexer.startIndexing();
		}
	}

	private IndexerBase getIndexer(final String coreName, String dataType,
			String sourceFile) throws UnsupportedCoreException {
		if (SolrConstants.CORE_NAME_CHINESE.equalsIgnoreCase(coreName)) {
			return new ChineseIndexer(dataType, sourceFile);
		} else {
			throw new UnsupportedCoreException(coreName);
		}
	}

	private void printUsage() {
		System.out.println("Usage: java -Xms500m -Xmx1500m -cp ./indexer.jar "
				+ IndexerMain.class.getPackage()
				+ " core_name data_type [index_source_file]");
	}

	public static void main(final String[] args) {
		final IndexerMain instance = new IndexerMain();

		String coreName = args[0];
		if (!coreName.startsWith("core-")) {
			coreName = "core-" + coreName;
		}

		String dataType = args[1].trim().toLowerCase();
		if (dataType.equals("json") || dataType.equals("xml")
				|| dataType.equals("csv")) {
			if (args.length != 3) {
				instance.printUsage();
				System.exit(1);
			}
			String sourceFile = args[2].trim();
			try {
				instance.run(coreName, dataType, sourceFile);
				System.exit(0);
			} catch (final Exception e) {
				// Don't use e.getMessage(), sometimes it returns null
				System.err.println("Indexer for core: " + coreName
						+ ", failed. Error: " + e);
				e.printStackTrace();
				System.exit(2);
			}
		} else if (dataType.equals("mysql")) {
			try {
				instance.run(coreName, dataType, null);
				System.exit(0);
			} catch (final Exception e) {
				// Don't use e.getMessage(), sometimes it returns null
				System.err.println("Indexer for core: " + coreName
						+ ", failed. Error: " + e);
				e.printStackTrace();
				System.exit(2);
			}
		}

	}
}
