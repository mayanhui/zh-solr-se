package zh.solr.se.indexer.db.dao;

/***
 * A DbRowListener is called each time a DB row is retrieved from database.
 * Subclasses should override method of gotDbRow. It should call super.gotDbRow()
 * if the row count is needed.
 */

public abstract class DbRowListener<T> {
	private int maxRows = -1;
	private int count = 0;
	
	protected DbRowListener() {
		this(-1);
	}
	
	protected DbRowListener(int maxRows) {
		this.maxRows = maxRows;
	}
	
	/**
	 * This method will be called every time a DB row is retrieved
	 * Subclass must implement this method. The only implementation the base class'
	 * provides is counting the rows
	 * @param city the city entity object
	 */
	public void gotDbRow(T entity) {
		if (entity != null)
			count++;
	}
	
	/**
	 * The default case is to retrieve all locations, in which case, maxRows <= 0.
	 * For debugging purposes, one may set maxRows to a small positive integer, such as 10.
	 * @return
	 */
	public int getMaxRows() {
		return maxRows;
	}
	
	/**
	 * This method returns the actual row count retrieved from database.
	 * @return
	 */
	public int getCount() {
		return count;
	}
}
