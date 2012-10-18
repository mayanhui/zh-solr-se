package zh.solr.se.indexer.db.entity;

public class ChineseEntity {
	private int id;
	private String name;
	private String content;
	
	public ChineseEntity() {
		super();
	}
	
	public ChineseEntity(int id, String name, String content) {
		super();
		this.id = id;
		this.name = name;
		this.content = content;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
