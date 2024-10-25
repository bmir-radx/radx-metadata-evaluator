package bmir.radx.metadata.evaluator.sharedComponents;

import java.util.HashMap;
import java.util.Map;

public class DistributionManager {
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

  public static void updateDistribution(Integer count, Map<Integer, Integer> distribution){
    distribution.merge(count, 1, Integer::sum);
  }

  public static void updateDistribution(String term, Map<String, Integer> distribution){
    distribution.merge(term, 1, Integer::sum);
  }

  public static void updateDistribution(Map<String, Integer> ctFreq, Map<String, Integer> distribution){
    ctFreq.forEach((ct, freq) -> distribution.merge(ct, freq, Integer::sum));
  }
}
