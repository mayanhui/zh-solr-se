package zh.solr.se.indexer.db.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/***
 * This class manages multiple update without opening and closing the connection for each update.
 */
public class BatchUpdateManager {
	DaoBase dao;
	private Connection connection;
	private Statement statement;
	
	public BatchUpdateManager(DaoBase dao) {
		if (dao == null)
			throw new NullPointerException("The DAO object for batch update manager must not be null.");
		
		this.dao = dao;
	}
	
	public Statement startBatch() throws SQLException {
		connection = dao.getConnection();
		statement = connection.createStatement();
		
		return statement;
	}
	
	public void executeUpdate(String updateQuery) throws SQLException {
		if (connection == null || statement == null)
			startBatch();
		
		statement.executeUpdate(updateQuery);
	}
	
	public void endBatch() {
		DaoBase.closeStatement(statement);
		DaoBase.closeConnection(connection);
		statement = null;
		connection = null;
	}
	
	public DaoBase getDao() {
		return dao;
	}
}
