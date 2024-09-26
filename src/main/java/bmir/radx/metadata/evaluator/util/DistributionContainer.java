package bmir.radx.metadata.evaluator.util;

import java.util.HashMap;
import java.util.Map;

public class DistributionContainer {
//  public static Map<String, Integer> initiateIntMap(){
//    var completeness = new HashMap<String, Integer>();
//    completeness.put("0%-20%", 0);
//    completeness.put("20%-40%", 0);
//    completeness.put("40%-60%", 0);
//    completeness.put("60%-80%", 0);
//    completeness.put("80%-100%", 0);
//    return completeness;
//  }

  public static Map<Integer, Integer> initiateIntMap(){
    return new HashMap<>();
  }

  public static Map<String, Integer> initiateStringMap(){
    return new HashMap<>();
  }

  public static void updateDistribution(Integer completeFieldsNumber, Map<Integer, Integer> distribution){
    distribution.merge(completeFieldsNumber, 1, Integer::sum);
  }

  public static void updateDistribution(Map<String, Integer> ctFreq, Map<String, Integer> distribution){
    ctFreq.forEach((ct, freq) -> distribution.merge(ct, freq, Integer::sum));
  }


//  public static void updateDistribution(double rate, Map<String, Integer> completeness){
//    String requiredRange = getRange(rate);
//    completeness.put(requiredRange, completeness.get(requiredRange) + 1);
//  }

//  private static String getRange(double rate) {
//    if (rate >= 0 && rate < 20) {
//      return "0%-20%";
//    } else if (rate >= 20 && rate < 40) {
//      return "20%-40%";
//    } else if (rate >= 40 && rate < 60) {
//      return "40%-60%";
//    } else if (rate >= 60 && rate < 80) {
//      return "60%-80%";
//    } else if (rate >= 80 && rate <= 100) {
//      return "80%-100%";
//    } else {
//      throw new IllegalArgumentException("Rate out of expected range: " + rate);
//    }
//  }
}
