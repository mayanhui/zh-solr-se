package zh.solr.se.indexer.chinese;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.solr.common.SolrInputDocument;
import org.codehaus.jackson.map.ObjectMapper;

import zh.solr.se.indexer.IndexerBase;
import zh.solr.se.indexer.db.entity.ChineseEntity;
import zh.solr.se.indexer.solrproxy.SolrConstants;
import zh.solr.se.indexer.solrproxy.UnsupportedCoreException;

public class ChineseIndexer extends IndexerBase {

	private String sourceFile;
	
	public ChineseIndexer(String dataType) throws UnsupportedCoreException {
		super(SolrConstants.CORE_NAME_CHINESE, dataType);
	}
	
	public ChineseIndexer(String dataType, String sourceFile) throws UnsupportedCoreException {
		super(SolrConstants.CORE_NAME_CHINESE, dataType);
		this.setSourceFile(sourceFile);
	}

	@Override
	protected String getFieldBoostsPropertyName() {
		return "";
	}

	@Override
	public int indexAllDocuments() throws Exception {
		if (super.dataType.equals(DataType.JSON)
				|| super.dataType.equals(DataType.XML)
				|| super.dataType.equals(DataType.CSV)) {
			return indexDocumentsFromFile();
		} else if (super.dataType.equals(DataType.MYSQL)) {
			return indexDocumentsFromDB();
		}
		return -1;
	}

	public void setSourceFile(String sourceFile){
		this.sourceFile = sourceFile;
	}
	
	public String getSourceFile(){
		return this.sourceFile;
	}
	
	private int indexDocumentsFromFile() throws Exception{
		int count = 0;
		if(super.dataType.equals(DataType.JSON)){
			JsonProcesser processer = new JsonProcesser(new ObjectMapper());
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getSourceFile())));
			String line = null;
			while(null !=(line = br.readLine())){
				ChineseEntity entity = processer.parseDataModel(line.trim());
				// index entity
				SolrInputDocument solrDoc = toSolrDocument(entity);
				try {
					indexDocument(solrDoc);
					++count;
				} catch (final Exception e) {
					e.printStackTrace();
					// skip this document
					continue;
				}
				//print log
				if ((count % 1000) == 0)
					System.out.println("Already indexed " + count + " documents ... ");
			}
			br.close();
		}else if(super.dataType.equals(DataType.XML)){
		// add xml parse code here
			
		}else if(super.dataType.equals(DataType.CSV)){
		// add csv parse code here
			
		}
		
		return count;
	}
	
	private int indexDocumentsFromDB(){
		return -1;
	}

	private SolrInputDocument toSolrDocument(final ChineseEntity entity) {
		if (entity == null)
			return null;

		final SolrInputDocument solrDoc = new SolrInputDocument();

		// add the article fields
		addFields(solrDoc, entity);
		return solrDoc;
	}

	private void addFields(final SolrInputDocument solrDoc,
			final ChineseEntity entity) {
		if (solrDoc == null || entity == null)
			return;

		addSolrField(solrDoc, SolrConstants.FIELD_CHINESE_ID, entity.getId());
		addSolrField(solrDoc, SolrConstants.FIELD_CHINESE_NAME, entity.getName());
		addSolrField(solrDoc, SolrConstants.FIELD_CHINESE_CONTENT, entity.getContent());

	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws UnsupportedCoreException
	 */
	public static void main(final String[] args)
			throws UnsupportedCoreException {
		final ChineseIndexer indexer = new ChineseIndexer("json","/var/zh-solr-se/samples/movie-data.json");
		try {
			indexer.startIndexing();
			System.out.println("Indexing successfully.");
			System.exit(0);
		} catch (final Exception e) {
			System.out.println("Indexing error: " + e.getMessage());
			System.exit(1);
		}
	}
}
