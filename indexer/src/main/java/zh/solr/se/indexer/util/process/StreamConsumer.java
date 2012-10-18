package zh.solr.se.indexer.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamConsumer extends Thread {
	InputStream inputStream;
	String streamLabel;

	StreamConsumer(InputStream inputStream, String streamLabel) {
		assert (inputStream != null && streamLabel != null);

		this.inputStream = inputStream;
		this.streamLabel = streamLabel;
	}

	public void run() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(streamLabel + ">" + line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
