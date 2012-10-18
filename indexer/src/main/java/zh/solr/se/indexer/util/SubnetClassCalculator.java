package zh.solr.se.indexer.util;

public class SubnetClassCalculator {
  /**
		C, if IP span < 256
		B, if 256 <= IP span < 65536
		A, if IP span >= 65536
   */
  public static final String ABOVE_A_CLASS_ERROR = "ABOVE A CLASS";
  public static final String A_CLASS = "A";
  public static final String B_CLASS = "B";
  public static final String C_CLASS = "C";

  public static final long A_CLASS_UPPER_BOUND = (long) Math.pow(2.0, 24.0); //16777214;
  public static final long A_CLASS_LOWER_BOUND = (long) Math.pow(2.0, 16.0); // 65536
  public static final long B_CLASS_UPPER_BOUND = A_CLASS_LOWER_BOUND;
  public static final long B_CLASS_LOWER_BOUND = (long) Math.pow(2.0, 8.0); // 256
  public static final long C_CLASS_UPPER_BOUND = B_CLASS_LOWER_BOUND;
  public static final long C_CLASS_LOWER_BOUND = 0;

  public static String getSubnetClass(Long ip) {
    if(ip >= C_CLASS_LOWER_BOUND && ip < C_CLASS_UPPER_BOUND) {
      return C_CLASS;
    }
    if(ip >= B_CLASS_LOWER_BOUND && ip < B_CLASS_UPPER_BOUND) {
      return B_CLASS;
    }
    if(ip >= A_CLASS_LOWER_BOUND && ip < A_CLASS_UPPER_BOUND) {
      return A_CLASS;
    }
    return ABOVE_A_CLASS_ERROR;
  }

  public static String getSubnetClass(Long endIp, Long startIp) {
    return getSubnetClass(endIp - startIp);
  }
}
