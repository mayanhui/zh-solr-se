package zh.solr.se.searcher.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static SimpleDateFormat df = new SimpleDateFormat(TIME_FORMAT);

	public static String formatCurrentTime() throws ParseException {
		Date date = new Date();
		return df.format(date);
	}

	public static void main(String[] args) throws ParseException {
		String str = formatCurrentTime();
		System.out.println(str);
	}
}
