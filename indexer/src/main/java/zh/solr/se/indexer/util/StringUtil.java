package zh.solr.se.indexer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class StringUtil {
  public static final String NEW_LINE = System.getProperty("line.separator");

  private static final String[] shortWordArray = {"of", "for", "in", "at", "on", "as"};
  private static HashSet<String> shortWords = new HashSet<String>();

  static {
    shortWords.addAll(Arrays.asList(shortWordArray));
  }

  /**
   * This method capitalize the first letter of the every words in the specified string
   *
   * @param value the string to capitalized
   * @return the result string
   */
  public static String capitalizeWords(String value) {
    return capitalizeWords(value, true);
  }

  /**
   * This method capitalize the first letter of the every words in the specified string
   *
   * @param value             the string to capitalized
   * @param excludeShortWords if true the short words will not be capitalized
   * @return the result string, the delimiter is kept
   */
  public static String capitalizeWords(String value, boolean excludeShortWords) {
    if(value == null || value.trim().length() == 0)
      return value;

    List<String> wordList = stringToStringList(value.toLowerCase(), "[ ]");
    for(int i = 0; i < wordList.size(); i++) {
      String word = wordList.get(i);
      String capitalizedWord = null;
      if(excludeShortWords && shortWords.contains(word))
        capitalizedWord = word;
      else
        capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
      wordList.set(i, capitalizedWord);
    }

    return listToString(wordList, " ");
  }

  /**
   * Convert an array of strings to a single string
   *
   * @param strArray the string array
   * @return the comma separated result string
   */
  public static String arrayToString(String[] strArray) {
    if(strArray == null || strArray.length == 0)
      return "";

    StringBuilder result = new StringBuilder();
    int lastIndex = strArray.length - 1;
    for(int i = 0; i < lastIndex; i++) {
      result.append(strArray[i]).append(", ");
    }
    result.append(strArray[lastIndex]);

    return result.toString();
  }

  /**
   * Convert an list of strings to a single string, the delimiter is ", ".
   *
   * @param valueList the string list
   * @return the comma separated result string
   */
  public static String listToString(List<String> valueList) {
    return listToString(valueList, ", ");
  }

  /**
   * Convert an list of strings to a single string
   *
   * @param valueList the string list
   * @param delimiter is inserted between two consecutive strings
   * @return the comma separated result string
   */
  public static String listToString(List<String> valueList, String delimiter) {
    if(valueList == null || valueList.size() == 0)
      return "";

    if(delimiter == null)
      delimiter = ", ";

    StringBuilder result = new StringBuilder();
    int lastIndex = valueList.size() - 1;
    for(int i = 0; i < lastIndex; i++) {
      result.append(valueList.get(i)).append(delimiter);
    }
    result.append(valueList.get(lastIndex));

    return result.toString();
  }

  /**
   * Parse a single string into a list of strings separated by the specified
   * delimiter. Empty strings are discarded
   *
   * @param valueStr  the single string that contains a list of strings separated by
   *                  a specified delimiter.
   * @param delimiter the string used as the delimiter
   * @return an array of strings
   */
  public static List<String> stringToStringList(String valueStr, String delimiter) {
    return stringToStringList(valueStr, delimiter, false);
  }

  /**
   * Parse a single single string into a list of strings separated by the specified
   * delimiter.
   *
   * @param valueStr         the single string that contains a list of strings separated by
   *                         a specified delimiter.
   * @param delimiter        the string used as the delimiter
   * @param keepEmptyStrings if true, keep the empty strings between delimiters
   * @return an array of strings
   */
  public static List<String> stringToStringList(String valueStr, String delimiter, boolean keepEmptyStrings) {
    if(valueStr == null)
      return null;

    // default delimiter is space and comma
    if(delimiter == null)
      delimiter = "[ ,]";

    String[] strArray = null;
    if(keepEmptyStrings)
      strArray = valueStr.split(delimiter, -1);
    else
      strArray = valueStr.split(delimiter);

    ArrayList<String> result = new ArrayList<String>();
    for(String aStrArray : strArray) {
      String word = aStrArray.trim();
      if(keepEmptyStrings || word.length() > 0)
        result.add(word);
    }

    return result;
  }

  /**
   * Reverse the order of a list of strings and then convert it to a single string
   *
   * @param strList the string list
   * @return the comma separated result string
   */
  public static String listToStringReverse(List<String> strList) {
    if(strList == null || strList.size() == 0)
      return "";

    int lastIndex = strList.size() - 1;
    StringBuilder result = new StringBuilder(strList.get(lastIndex));
    for(int i = lastIndex - 1; i >= 0; i--) {
      result.append(", ").append(strList.get(i));
    }

    return result.toString();
  }

  /**
   * Construct the regex string from the given delimiter string
   * This is used in String.split(regex)
   *
   * @param delimiterStr delimiter string
   * @return the regex string
   */
  public static String getRegExDelimiter(String delimiterStr) {
    StringBuilder rexBuilder = new StringBuilder();
    for(int i = 0; i < delimiterStr.length(); i++) {
      rexBuilder.append('[').append(delimiterStr.charAt(i)).append(']');
    }

    return rexBuilder.toString();
  }

  public static List<Integer> parseNumberList(String valueList, String delimiter) {
    if(valueList == null)
      return null;

    ArrayList<Integer> resultList = new ArrayList<Integer>();
    String[] valueArray = valueList.split(delimiter);
    int i = 0;
    while(i < valueArray.length) {
      try {
        int value = Integer.parseInt(valueArray[i]);
        resultList.add(value);
      }
      catch(Exception e) {
        // do nothing
      }
      i++;
    }

    return resultList;
  }

  public static boolean stringEqual(String str1, String str2) {
    if(str1 == null)
      return (str2 == null);

    return str1.equalsIgnoreCase(str2);
  }


  /**
   * Not exactly sure how this is supposed to work.  Nothing is using it.  I
   * wrote a test for it but I'm not sure if the replacement of & should
   * happen before everything or after everything.  If it happens after then
   * you'll end up with &amp;lt; and I'm not sure that's correct.
   * <p/>
   * There was a bug in the original version as value was not getting reassigned
   * So with each replace the replacement happened on the original value and
   * then was lost.  The final result is that the output is the same as the
   * input.
   *
   * @param value string to escape
   * @return escaped string
   */
  public static String escapeXmlSpecialCharacters(String value) {
    if(value == null)
      return null;

    value = value.replace("&", "&amp;");
    value = value.replace("<", "&lt;");
    value = value.replace(">", "&gt;");
    value = value.replace("\"", "&quot;");
    value = value.replace("\'", "&apos;");

    return value;
  }

  public static String tripPunctuations(String origStr) {
    if(origStr == null)
      return null;

    String newStr = origStr.replaceAll("[.`\"\']", "");
    newStr = newStr.replaceAll("[/]", " ");
    newStr = newStr.replace('\\', ' ');

    return newStr;
  }

  /**
   * e.g. originalStr = "a,b,c,d,e,f", delimiter = "," , count=3, return="a,b,c"
   *
   * @param origStr   delimited string
   * @param delimiter delimiter in the original and result
   * @param count     how many of the original elements to include in the result
   * @return returns string with specified values
   */
  public static String cutOffDelimitedString(String origStr, String delimiter, int count) {
    if(origStr == null && count == 0)
      return null;
    String[] strArray = origStr.split(delimiter);
    StringBuilder strBuilder = new StringBuilder(strArray[0]);
    if(count > strArray.length)
      count = strArray.length;
    for(int i = 1; i < count; i++) {
      strBuilder.append(delimiter).append(strArray[i]);

    }
    return strBuilder.toString();
  }

  /**
   * Return true if value is null or when trimmed has zero length.
   *
   * @param value the value to test
   * @return true if null or trimmed value has zero length
   */
  public static boolean isNullOrEmpty(String value) {
    return value == null || value.trim().length() == 0;
  }

  public static void main(String[] argv) {
  }

}
