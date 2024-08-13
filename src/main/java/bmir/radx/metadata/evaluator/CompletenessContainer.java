package bmir.radx.metadata.evaluator;

import java.util.HashMap;
import java.util.Map;

public class CompletenessContainer {
  public static Map<String, Integer> initiateCompletenessMap(){
    var completeness = new HashMap<String, Integer>();
    completeness.put("0%-20%", 0);
    completeness.put("20%-40%", 0);
    completeness.put("40%-60%", 0);
    completeness.put("60%-80%", 0);
    completeness.put("80%-100%", 0);
    return completeness;
  }

  public static void updateCompletenessDistribution(double rate, Map<String, Integer> completeness){
    String requiredRange = getRange(rate);
    completeness.put(requiredRange, completeness.get(requiredRange) + 1);
  }

  private static String getRange(double rate) {
    if (rate >= 0 && rate < 20) {
      return "0%-20%";
    } else if (rate >= 20 && rate < 40) {
      return "20%-40%";
    } else if (rate >= 40 && rate < 60) {
      return "40%-60%";
    } else if (rate >= 60 && rate < 80) {
      return "60%-80%";
    } else if (rate >= 80 && rate <= 100) {
      return "80%-100%";
    } else {
      throw new IllegalArgumentException("Rate out of expected range: " + rate);
    }
  }
}
