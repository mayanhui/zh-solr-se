package zh.solr.se.indexer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UUIDGenerator {
	private List<String> items = new ArrayList<String>();

	public String getUUID() {
		String items = toString();
		return UUID.nameUUIDFromBytes(items.getBytes()).toString();
	}


	public void add(Object item) {
		String stringItem = "";
		if(item != null) {
			stringItem = item.toString();
		}
		add(stringItem);
	}

	public void add(String item) {
		if(StringUtil.isNullOrEmpty(item)) {
			item = "";
		}
		items.add(item);
	}

	public int size() {
		return items.size();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(String item : items) {
			builder.append(item);
		}
		return builder.toString();
	}
}
