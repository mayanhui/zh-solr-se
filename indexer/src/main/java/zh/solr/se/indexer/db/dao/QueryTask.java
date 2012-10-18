package zh.solr.se.indexer.db.dao;

import java.sql.ResultSet;

public abstract class QueryTask {
	private String query;
	
	public QueryTask(String query) {
		this.query = query;
	}
	
	public abstract void processResultSet(ResultSet resultSet) throws Exception ;
	
	public String getQuery() {
		return query;
	}
}
