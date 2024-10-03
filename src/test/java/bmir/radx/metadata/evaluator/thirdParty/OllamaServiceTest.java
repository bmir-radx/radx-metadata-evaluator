package bmir.radx.metadata.evaluator.thirdParty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class OllamaServiceTest {
  @Autowired
  private OllamaService ollamaService;

  @Test
  public void testApiCall() {
    String t1 = "Personalized Analytics and Wearable Biosensor Platform for Early Detection of COVID-19 Decompensation (DECODE)";
    String t2 = "Detection of COVID-19 Decompensation (DeCODe)";
    String d1 = "";
//    String d2 = "In this study we will be monitoring for patient events (emergency department admission, hospital admission, admission to an observation unit, or death) and evaluating the feasibility and utility of using pinpointIQ in the management of patients with COVID-19. Vital sign (physiology data) is collected to build a Covid Decompensation Index and contribute data to a Covid Digital Hub supported by the National Institutes of Health." +
//        "This is a prospective, non-randomized, open-label, two-phase design. The primary focus for the study is data collection for index development. This will be done in two phases: the first phase allows for determination of predictor variables that establish the COVID-19 Decompensation Index (CDI) and the second phase establishes performance of the CDI. A participant is considered to have completed the study if he or she completes all phases of the study including the last day of monitoring (day 28).";

    String d2 = d1;
    var response = ollamaService.sendRequest(t1, t2, d1, d2);
    assertNotNull(response, "Response should not be null");
  }

}
