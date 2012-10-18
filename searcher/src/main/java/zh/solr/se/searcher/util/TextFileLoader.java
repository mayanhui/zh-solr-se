package zh.solr.se.searcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextFileLoader {
	public static void load(String path, TextLineListener listener) {
		if (path == null)
			throw new NullPointerException("The text file path must not be null");
		if (listener == null)
			throw new NullPointerException("The text line listener for file: " + path + ", must not be null");
		
		BufferedReader reader = null;
		try {
			InputStream inStream = TextFileLoader.class.getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(inStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				listener.gotLine(line);
			}
		} catch (Exception e) {
			System.err.println("Failed to load text file: " + path + ", error: " + e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}
}
