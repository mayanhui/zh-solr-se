package zh.solr.se.indexer.db.dao;

import java.sql.Statement;

public abstract class UpdateTask {
	public abstract void execute(Statement statement) throws Exception;
}
