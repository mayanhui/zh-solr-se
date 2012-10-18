package zh.solr.se.indexer.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import zh.solr.se.indexer.db.DbService;


public abstract class DaoBase {
	protected DbService dbService;
	
	public DaoBase() {
		this(null);
	}
	
	public DaoBase(String url) {
		try {
			dbService = DbService.getInstance();
			if (url != null) {
				
			}
		} catch (Exception e) {
			dbService = null;
			throw new RuntimeException("Failed to get a instance of DB service, error: " + e.getMessage());
		}
	}

	protected abstract Connection getConnection();
	
	public void executeQuery(QueryTask queryTask) throws Exception {
		if (queryTask == null)
			return;
		
		Connection conn = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			// make the connection to DB
			conn = getConnection();
			statement = conn.prepareStatement(queryTask.getQuery());			
			statement.setFetchSize(Integer.MIN_VALUE);
			resultSet = statement.executeQuery();
			queryTask.processResultSet(resultSet);
		} finally {
			// close the connection
			closeResultSet(resultSet);
			closeStatement(statement);
			closeConnection(conn);
		}
	}
	
	
	public void executeUpdate(UpdateTask updateTask) throws Exception {
		if (updateTask == null)
			return;
		
		Connection conn = null;
		Statement statement = null;
		try {
			// make the connection to DB
			conn = getConnection();
			statement = conn.createStatement();
			updateTask.execute(statement);
		} finally {
			// close the connection
			closeStatement(statement);
			closeConnection(conn);
		}
	}

	protected static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) {
				// do nothing
			}
		}
	}

	protected static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
				// do nothing
			}
		}
	}

	protected static void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException sqlEx) {
				// do nothing
			}
		}
	}
}
