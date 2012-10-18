package zh.solr.se.indexer;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;

import zh.solr.se.indexer.solrproxy.LocalSolrServer;
import zh.solr.se.indexer.solrproxy.LocalSolrServerFactory;
import zh.solr.se.indexer.solrproxy.UnsupportedCoreException;
import zh.solr.se.indexer.util.IndexerUtil;


public abstract class IndexerBase {
	
	protected enum DataType {
		XML("xml"), JSON("json"), CSV("csv"), MYSQL("mysql");

		private String value;

		public String getValue() {
			return value;
		}

		private DataType(String value) {
			this.value = value;
		}
	}
	
	protected DataType dataType;
	private final LocalSolrServer solrServer;
	private Map<String, Float> fieldBoostMap = new HashMap<String, Float>();

	protected IndexerBase(final String coreName, String dataType)
			throws UnsupportedCoreException {
		solrServer = LocalSolrServerFactory.getInstance().getLocalSolrServer(
				coreName);
		fieldBoostMap = IndexerUtil
				.readFieldBoostsFromConfig(getFieldBoostsPropertyName());
		setDataType(dataType);
	}
	
	protected void setDataType(String dataType){
		if(null != dataType){
			if(dataType.equals(DataType.XML.getValue())){
				this.dataType = DataType.XML;
			}else if(dataType.equals(DataType.JSON.getValue())){
				this.dataType = DataType.JSON;
			}else if(dataType.equals(DataType.CSV.getValue())){
				this.dataType = DataType.CSV;
			}else if(dataType.equals(DataType.MYSQL.getValue())){
				this.dataType = DataType.MYSQL;
			}
		}
	}
	
	protected abstract String getFieldBoostsPropertyName();

	protected abstract int indexAllDocuments() throws Exception;

	public void startIndexing() throws Exception {
		final long time0 = System.currentTimeMillis();
		System.out.println("Indexer started ...");

		// retrieve all documents and index them
		final int count = indexAllDocuments();

		// optimize the index
		solrServer.optimize();

		// commit the changes
		// solrServer.commit();

		// shutdown the server
		solrServer.close();

		System.out.println("Indexer done. Total documents = " + count);

		// report time
		final long time1 = System.currentTimeMillis();
		final int[] timeValues = getTimeValues(time0, time1);
		System.out.println("Indexing took " + timeValues[0] + " hours, "
				+ timeValues[1] + " minuts, " + timeValues[2] + " seconds.");
	}

	public void indexDocument(final SolrInputDocument solrDoc) throws Exception {
		if (solrDoc != null) {
			solrServer.addDocument(solrDoc);
		}
	}

	public void addSolrField(final SolrInputDocument solrDoc,
			final String name, final Object value) {
		addSolrField(solrDoc, name, value, false);
	}

	public void addSolrField(final SolrInputDocument solrDoc,
			final String name, Object value, final boolean convertToString) {
		if ((solrDoc == null) || (name == null) || (value == null)) {
			return;
		}

		if (convertToString) {
			value = value.toString();
		}

		// set the field boost according to values in config file
		final Float boost = fieldBoostMap.get(name);
		if (boost != null) {
			solrDoc.addField(name, value, boost);
		} else {
			solrDoc.addField(name, value);
		}
	}

	private int[] getTimeValues(final long time0, final long time1) {
		final int totalSeconds = (int) ((time1 - time0) / 1000);
		final int totalMinutes = totalSeconds / 60;
		final int hours = totalMinutes / 60;
		final int minutes = totalMinutes % 60;
		final int seconds = totalSeconds % 60;

		final int[] timeValues = { hours, minutes, seconds };

		return timeValues;
	}

}
