package bmir.radx.metadata.evaluator.sharedComponents;

import java.util.ArrayList;
import java.util.List;

public class LinguisticQualityChecker {
  public static List<Integer> checkExtraSpace(String input){
    List<Integer> indexes = new ArrayList<>();

    if (input == null || input.isEmpty()) {
      return indexes; // Return empty list if input is null or empty
    }

    // Check for leading spaces
    if (Character.isWhitespace(input.charAt(0))) {
      indexes.add(0);
    }

    // Check for trailing spaces
    if (Character.isWhitespace(input.charAt(input.length() - 1))) {
      indexes.add(input.length() - 1);
    }

    // Check for consecutive spaces in the middle
    for (int i = 0; i < input.length() - 1; i++) {
      if (input.charAt(i) == ' ' && input.charAt(i + 1) == ' ') {
        indexes.add(i);
      }
    }

    return indexes;
  }
}
