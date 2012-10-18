package zh.solr.se.indexer.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/***
 * This class manages multiple query without opening and closing the connection for each query.
 */
public class BatchQueryManager {
	DaoBase dao;
	private Connection connection;
	private Statement statement;
	
	public BatchQueryManager(DaoBase dao) throws SQLException {
		if (dao == null)
			throw new NullPointerException("The DAO object for batch query manager must not be null.");
		this.dao = dao;
		initial();
	}
	
	private void initial() throws SQLException {
		connection = dao.getConnection();
		statement = connection.createStatement();
	}
	
	public ResultSet executeQuery(String query) throws SQLException {
		if (connection == null || statement == null)
			initial();
		return statement.executeQuery(query);
	}
	
	public void setFetchSize(int size) throws SQLException{
		if(null == statement)
			return;
		statement.setFetchSize(size);
	}
	
	public ResultSet executeQuery(PreparedStatement pstmt, Object... params) throws SQLException {
		if(params != null && params.length > 0) {
			for(int i=1; i<=params.length;i++) {
				pstmt.setObject(i, params[i-1]);
			}
		}
		return pstmt.executeQuery();
	}
	
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if(connection== null)
			connection = this.dao.getConnection();
		return connection.prepareStatement(sql);
	}
	
	public void close(PreparedStatement... pstmts) {
		if(null != pstmts) {
			for(PreparedStatement stmt : pstmts) {
				DaoBase.closeStatement(stmt);
				stmt = null;
			}
			pstmts = null;
		}
		DaoBase.closeStatement(statement);
		DaoBase.closeConnection(connection);
		statement = null;
		connection = null;
	}
	
	public DaoBase getDao() {
		return dao;
	}
}
