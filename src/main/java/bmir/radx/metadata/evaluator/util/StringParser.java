package bmir.radx.metadata.evaluator.util;

import java.util.Map;
import java.util.TreeMap;

public class StringParser {
  public static Map<?, Integer> parseToMap(String distributionText) {
    distributionText = distributionText.replaceAll("[\\{\\}]", "");
    String[] pairs = distributionText.split(",\\s*");
    String firstKey = pairs[0].split("=")[0].trim();

    try {
      // Try parsing the first key as an Integer
      Integer.parseInt(firstKey);
      return parseToIntegerMap(distributionText);
    } catch (NumberFormatException e) {
      // If it fails, it's a String key
      return parseToStringMap(distributionText);
    }
  }
  public static Map<Integer, Integer> parseToIntegerMap(String distributionText) {
    distributionText = distributionText.replaceAll("[\\{\\}]", "");
    String[] pairs = distributionText.split(",\\s*");
    Map<Integer, Integer> map = new TreeMap<>();
    int maxKey = 0;
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      int key = Integer.parseInt(keyValue[0].trim());
      int value = Integer.parseInt(keyValue[1].trim());
      map.put(key, value);
      if (key > maxKey) {
        maxKey = key;
      }
    }
    // Ensure all keys from 0 to maxKey are present, with default value 0 if missing
    for (int i = 0; i <= maxKey; i++) {
      map.putIfAbsent(i, 0);
    }

    return map;
  }

  public static Map<String, Integer> parseToStringMap(String distributionText) {
    distributionText = distributionText.replaceAll("[\\{\\}]", "");
    String[] pairs = distributionText.split(",\\s*");
    Map<String, Integer> map = new TreeMap<>();
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      String key = keyValue[0].trim();
      int value = Integer.parseInt(keyValue[1].trim());
      map.put(key, value);
    }
    return map;
  }
}
