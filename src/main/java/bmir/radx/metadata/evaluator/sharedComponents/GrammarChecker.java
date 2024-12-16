package bmir.radx.metadata.evaluator.sharedComponents;

import org.languagetool.Languages;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GrammarChecker {
  public void check(String text, String phs, String fieldName) throws IOException {
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("en-GB"));
    List<RuleMatch> matches = langTool.check(text);

    for (RuleMatch match : matches) {
      System.out.println("Potential issue in Study " + phs + " '" + fieldName + "' at characters " +
          match.getFromPos() + "-" + match.getToPos() + ": " +
          match.getMessage());
      System.out.println("Suggested correction(s): " +
          match.getSuggestedReplacements());
    }
  }
}
